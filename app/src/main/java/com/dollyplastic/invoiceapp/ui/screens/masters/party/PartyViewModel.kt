package com.dollyplastic.invoiceapp.ui.screens.masters.party

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.data.repository.PartyRepository
import com.dollyplastic.invoiceapp.domain.Usecase.AddPartyUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import javax.inject.Inject
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Validation.PartyValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import com.dollyplastic.invoiceapp.domain.config.StateConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val repository: PartyRepository,
    private val firmRepository: com.dollyplastic.invoiceapp.data.repository.FirmRepository,
    private val distanceRepository: com.dollyplastic.invoiceapp.data.repository.DistanceRepository,
    private val addPartyUseCase: AddPartyUseCase
) : ViewModel() {

    /* ---------- LIST ---------- */

    private val _parties = MutableStateFlow<List<Party>>(emptyList())
    val parties = _parties.asStateFlow()

    private val _firms = MutableStateFlow<List<com.dollyplastic.invoiceapp.data.models.Firm>>(emptyList())
    val firms = _firms.asStateFlow()

    /* ---------- FORM ---------- */

    private val _formState = MutableStateFlow(PartyFormState())
    val formState = _formState.asStateFlow()

    /* ---------- UI EVENTS ---------- */

    sealed class PartyUiEvent {
        data class ShowErrorDialog(val message: String) : PartyUiEvent()
        data object ShowConfirmDialog : PartyUiEvent()
    }


    private val _uiEvent = MutableSharedFlow<PartyUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val isFormValid =
        formState
            .map { state ->
                val errors = PartyValidator.validate(state.toParty(), ValidationLevel.BASE)
                errors.isEmpty() && !state.isLoadingDistances
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )


    private var initialState: PartyFormState? = null

    val isModified = _formState.map { current ->
        initialState?.let { initial ->
             areFormsDifferent(initial, current)
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private fun areFormsDifferent(initial: PartyFormState, current: PartyFormState): Boolean {
         if (initial.tradeName != current.tradeName) return true
         if (initial.nickName != current.nickName) return true
         if (initial.gstin != current.gstin) return true
         if (initial.addressLine1 != current.addressLine1) return true
         if (initial.addressLine2 != current.addressLine2) return true
         if (initial.city != current.city) return true
         if (initial.state != current.state) return true
         if (initial.pincode != current.pincode) return true
         if (initial.unknownPincodes != current.unknownPincodes) return true
         
         return false
    }

    private val _pincodeQuery = MutableStateFlow("")

    init {
        initialState = PartyFormState() // Default
        loadParties()
        loadFirms()
        
        viewModelScope.launch {
            _pincodeQuery
                .debounce(300L)
                .collectLatest { pin ->
                     if (pin.length == 6) {
                         recalculatePincodes(pin)
                     } else {
                         // Clear if invalid
                         _formState.update { it.copy(unknownPincodes = emptyList(), isLoadingDistances = false) }
                     }
                }
        }
    }

    private fun loadFirms() {
        viewModelScope.launch {
            val r = firmRepository.getAllFirms()
            if (r is Result.Success) {
                _firms.value = r.data
            }
        }
    }

    fun loadParties() {
        viewModelScope.launch {
            when (val r = repository.getAllParties()) {
                is Result.Success -> _parties.value = r.data
                is Result.Error -> _parties.value = emptyList()
            }
        }
    }

    fun deleteParty(partyId: String) {
        viewModelScope.launch {
            repository.deleteParty(partyId)
            loadParties()
        }
    }

    fun loadPartyForEdit(partyId: String) {
        viewModelScope.launch {
            when (val r = repository.getParty(partyId)) {
                is Result.Success -> {
                    val party = r.data
                    val state = PartyFormState.fromParty(party)
                    _formState.value = state
                    initialState = state

                    _pincodeQuery.value = state.pincode
                }
                else -> Unit
            }
        }
    }

    private suspend fun recalculatePincodes(partyPincode: String) {
        android.util.Log.d("DistanceDebug", "Checking distances for Client Pincode: $partyPincode")

        val currentState = _formState.value
        _formState.value = currentState.copy(isLoadingDistances = true)

        val firms = _firms.value
        val grouped = firms.groupBy { it.pincode }
        android.util.Log.d("DistanceDebug", "Found ${grouped.size} unique firm pincodes to check.")
        
        val newGroups = mutableListOf<com.dollyplastic.invoiceapp.ui.models.PincodeInputGroup>()
        
        grouped.forEach { (pin, firmList) ->
            if (pin.isNotBlank() && pin != partyPincode) {
                    val knownDist = distanceRepository.getPincodeDistance(partyPincode, pin)
                    
                    if (knownDist == null) {
                        android.util.Log.d("DistanceDebug", "MISS: Distance unknown for $pin. Requesting input.")
                        val existingInput = currentState.unknownPincodes.find { it.pincode == pin }
                        
                        newGroups.add(
                            com.dollyplastic.invoiceapp.ui.models.PincodeInputGroup(
                                pincode = pin,
                                entityNames = firmList.map { it.tradeName },
                                distance = existingInput?.distance ?: ""
                            )
                        )
                    } else {
                        android.util.Log.d("DistanceDebug", "HIT: Distance found for $pin: $knownDist km")
                    }
            }
        }

        android.util.Log.d("DistanceDebug", "Final missing pincodes to ask user: ${newGroups.map { it.pincode }}")
        
        _formState.update {
            it.copy(
                unknownPincodes = newGroups,
                isLoadingDistances = false
            )
        }
    }

    fun onFieldBlur(field: String) {
        val current = _formState.value
        val updated = current.copy(touchedFields = current.touchedFields + field)
        validateAndUpdate(updated)
    }

    private fun validateAndUpdate(state: PartyFormState) {
        val errors = PartyValidator
            .validate(state.toParty(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }
            .filterKeys { it in state.touchedFields }

        _formState.value = state.copy(errors = errors)
    }

    fun onFieldChange(field: String, value: String) {
        var updated = _formState.value.update(field, value)
        updated = updated.copy(touchedFields = updated.touchedFields + field)
        
        _formState.value = updated

        if (field == "gstin" && value.length >= 2) {
            updated = updated.copy(stateCode = value.take(2))
            _formState.value = updated // Update state code
        }
        
        if (field == "pincode") {
             _pincodeQuery.value = value
        }

        validateAndUpdate(updated)
    }

    fun onGstinChange(value: String) {
        var updated = _formState.value.update("gstin", value.uppercase())
        updated = updated.copy(touchedFields = updated.touchedFields + "gstin")

        if (value.length >= 2) {
            val stateCode = value.take(2)
            val state = StateConfig.getByCode(stateCode)

            if (state != null) {
                updated = updated.copy(
                    state = state.name,
                    stateCode = state.code
                )
            } else {
                updated = updated.copy(
                    state = "",
                    stateCode = ""
                )
            }
        } else {
            updated = updated.copy(
                state = "",
                stateCode = ""
            )
        }

        validateAndUpdate(updated)
    }

    fun onDistanceChange(pincode: String, value: String) {
        if (value.all { it.isDigit() }) {
           _formState.value = _formState.value.updateDistance(pincode, value)
        }
    }

    fun requestSaveConfirmation() {
        viewModelScope.launch {
            _uiEvent.emit(PartyUiEvent.ShowConfirmDialog)
        }
    }

    fun saveParty(onSuccess: () -> Unit) {
        viewModelScope.launch {
            var finalState = _formState.value
            if (finalState.partyId.isNullOrBlank()) {
                val newId = java.util.UUID.randomUUID().toString()
                finalState = finalState.copy(partyId = newId)
                _formState.value = finalState
            }

            val missingDistances = finalState.unknownPincodes.filter { 
                it.distance.isBlank() || (it.distance.toIntOrNull() ?: 0) <= 0 
            }

            if (missingDistances.isNotEmpty()) {
                val missingPins = missingDistances.joinToString(", ") { it.pincode }
                _uiEvent.emit(PartyUiEvent.ShowErrorDialog("Please enter distance for pincodes: $missingPins"))
                return@launch
            }

            val partyToSave = finalState.toParty()
            val result = addPartyUseCase.execute(partyToSave)

            when (result) {
                is ValidationResult.Valid -> {
                    finalState.unknownPincodes.forEach { group ->
                         val dist = group.distance.toIntOrNull() ?: 0
                         if (dist > 0) {
                              distanceRepository.setPincodeDistance(partyToSave.pincode, group.pincode, dist)
                         }
                    }

                    _formState.value = PartyFormState()
                    loadParties()
                    onSuccess()
                }
                is ValidationResult.Invalid -> {
                    val dup = result.errors.firstOrNull { it.field == "gstin" }
                    if (dup != null) {
                        _uiEvent.emit(PartyUiEvent.ShowErrorDialog(dup.message))

                    } else {
                        val errorFields = result.errors.map { it.field }.toSet()
                        _formState.value = finalState.copy(
                            touchedFields = finalState.touchedFields + errorFields,
                            errors = result.errors.associate {
                                it.field to it.message
                            }
                        )
                    }
                }
            }
        }
    }
}
