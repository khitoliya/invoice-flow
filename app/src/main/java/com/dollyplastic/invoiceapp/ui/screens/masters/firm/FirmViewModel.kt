package com.dollyplastic.invoiceapp.ui.screens.masters.firm


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Usecase.AddFirmUseCase
import com.dollyplastic.invoiceapp.domain.Validation.FirmValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import com.dollyplastic.invoiceapp.domain.config.StateConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import javax.inject.Inject

@HiltViewModel
class FirmViewModel @Inject constructor(
    private val repository: FirmRepository,
    private val partyRepository: com.dollyplastic.invoiceapp.data.repository.PartyRepository,
    private val distanceRepository: com.dollyplastic.invoiceapp.data.repository.DistanceRepository,
    private val addFirmUseCase: AddFirmUseCase
) : ViewModel() {


    private val _uiEvent = MutableSharedFlow<FirmUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    /* ---------------- LIST STATE ---------------- */

    private val _firms = MutableStateFlow<List<Firm>>(emptyList())
    val firms = _firms.asStateFlow()
    
    // Parties list for manual distance entry
    private val _parties = MutableStateFlow<List<com.dollyplastic.invoiceapp.data.models.Party>>(emptyList())
    val parties = _parties.asStateFlow()

    /* ---------------- FORM STATE ---------------- */
    private val _formState = MutableStateFlow(FirmFormState())
    val formState = _formState.asStateFlow()

    private val _pincodeQuery = MutableStateFlow("")

    private var initialState: FirmFormState? = null
    
    val isModified = _formState.map { current ->
        initialState?.let { initial ->
             // Compare essential fields only (ignore errors, loading, touched fields)
             // We can compare the resulting Firm objects
             val initialFirm = initial.toFirm()
             val currentFirm = current.toFirm()
             
             // Also need to check unknownPincodes (distances) if they are editable
             // The distances are part of the state but not the Firm object directly unless embedded?
             // Firm doesn't hold the 'unknown' distances.
             // But if user changes distances, it should trigger save.
             // Wait, where are distances stored? In data/models/firm? No/Yes?
             // Firm has 'pincode', but not the distances to others.
             // BUT, the form allows editing distances which are stored in PincodeDistance repo.
             // So if those change, we should allow save.
             // Let's compare raw fields for now.
             
             areFormsDifferent(initial, current)
        } ?: false
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private fun areFormsDifferent(initial: FirmFormState, current: FirmFormState): Boolean {
         if (initial.tradeName != current.tradeName) return true
         if (initial.nickName != current.nickName) return true
         if (initial.gstin != current.gstin) return true
         if (initial.addressLine1 != current.addressLine1) return true
         if (initial.addressLine2 != current.addressLine2) return true
         if (initial.city != current.city) return true
         if (initial.state != current.state) return true
         if (initial.pincode != current.pincode) return true
         if (initial.bankName != current.bankName) return true
         if (initial.accountNumber != current.accountNumber) return true
         if (initial.ifscCode != current.ifscCode) return true
         if (initial.branchName != current.branchName) return true
         
         // Helper for distances:
         // If unknownPincodes changed content (distance value)
         // We must compare the distances for the SAME pincodes.
         // Or simpler: compare the lists.
         if (initial.unknownPincodes != current.unknownPincodes) return true
         
         return false
    }

    init {
        initialState = FirmFormState() // Default for new entry
        loadFirms()
        loadParties()
        
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
    
    private fun loadParties() {
        viewModelScope.launch {
            val r = partyRepository.getAllParties()
            if (r is Result.Success) {
                _parties.value = r.data
            }
        }
    }

    /* ---------------- LIST LOGIC ---------------- */

    fun loadFirms() {
        viewModelScope.launch {
            when (val result = repository.getAllFirms()) {
                is Result.Success -> _firms.value = result.data
                is Result.Error -> _firms.value = emptyList()
            }
        }
    }

    fun deleteFirm(firmId: String) {
        viewModelScope.launch {
            repository.deleteFirm(firmId)
            loadFirms()
        }
    }


    /* ---------------- FORM LOGIC ---------------- */

    fun loadFirmForEdit(firmId: String) {
        viewModelScope.launch {
            when (val result = repository.getFirm(firmId)) {
                is Result.Success -> {
                    val firm = result.data
                    val state = FirmFormState.fromFirm(firm)
                    _formState.value = state
                    initialState = state // Capture baseline for edit
                    
                    // Trigger initial calculation without debounce delay if needed, 
                    // or just emit to flow. Emitting is safer.
                    _pincodeQuery.value = state.pincode
                }
                is Result.Error -> Unit
            }
        }
    }
    
    private suspend fun recalculatePincodes(firmPincode: String) {
        android.util.Log.d("DistanceDebug", "Checking distances for Firm Pincode: $firmPincode")

        val currentState = _formState.value
        _formState.value = currentState.copy(isLoadingDistances = true)
        
        val parties = _parties.value
        val grouped = parties.groupBy { it.pincode }
        android.util.Log.d("DistanceDebug", "Found ${grouped.size} unique customer pincodes to check.")
        
        val newGroups = mutableListOf<com.dollyplastic.invoiceapp.ui.models.PincodeInputGroup>()
        
        grouped.forEach { (pin, partyList) ->
            if (pin.isNotBlank() && pin != firmPincode) {
                    // Check if distance is already known
                    val knownDist = distanceRepository.getPincodeDistance(firmPincode, pin)
                    
                    if (knownDist == null) {
                        android.util.Log.d("DistanceDebug", "MISS: Distance unknown for $pin. Requesting input.")
                        // Distance Unknown -> Add to Input Group (preserve user input if exists)
                        val existingInput = currentState.unknownPincodes.find { it.pincode == pin }
                        
                        newGroups.add(
                            com.dollyplastic.invoiceapp.ui.models.PincodeInputGroup(
                                pincode = pin,
                                entityNames = partyList.map { it.tradeName },
                                distance = existingInput?.distance ?: ""
                            )
                        )
                    } else {
                        android.util.Log.d("DistanceDebug", "HIT: Distance found for $pin: $knownDist km")
                    }
            }
        }

        android.util.Log.d("DistanceDebug", "Final missing pincodes to ask user: ${newGroups.map { it.pincode }}")
        
        // collectLatest automatically handles cancellation, no need to check isActive aggressively 
        // (though simple checks don't hurt, the main benefit is discarding old values)

        // Set Loading -> FALSE and Update List
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

    private fun validateAndUpdate(state: FirmFormState) {
        val errors = FirmValidator
            .validate(state.toFirm(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }
            .filterKeys { it in state.touchedFields }

        _formState.value = state.copy(errors = errors)
    }

    fun onFieldChange(field: String, value: String) {
        var updated = _formState.value.update(field, value)
        updated = updated.copy(touchedFields = updated.touchedFields + field)

        _formState.value = updated // Update UI immediately

        if (field == "pincode") {
             // Emit to stream
             _pincodeQuery.value = value
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
            _uiEvent.emit(FirmUiEvent.ShowConfirmDialog)
        }
    }

    fun saveFirm(onSuccess: () -> Unit) {
        viewModelScope.launch {
            // 1. Ensure ID exists or generate it ONCE
            var finalState = _formState.value
            if (finalState.firmId.isNullOrBlank()) {
                val newId = java.util.UUID.randomUUID().toString()
                finalState = finalState.copy(firmId = newId)
                _formState.value = finalState // Update state
            }

            // 2. Validate Distances (Mandatory Check)
            // Check that all displayed unknown pincode fields have a valid distance
            val missingDistances = finalState.unknownPincodes.filter { 
                it.distance.isBlank() || (it.distance.toIntOrNull() ?: 0) <= 0 
            }

            if (missingDistances.isNotEmpty()) {
                val missingPins = missingDistances.joinToString(", ") { it.pincode }
                _uiEvent.emit(FirmUiEvent.ShowErrorDialog("Please enter distance for pincodes: $missingPins"))
                return@launch
            }

            // 3. Save Firm
            val firmToSave = finalState.toFirm()
            val result = addFirmUseCase.execute(firmToSave)

            when (result) {
                is ValidationResult.Invalid -> {

                    val duplicateError = result.errors.firstOrNull {
                        it.field == "gstin"
                    }

                    if (duplicateError != null) {
                        _uiEvent.emit(
                            FirmUiEvent.ShowErrorDialog(
                                duplicateError.message
                            )
                        )
                        return@launch
                    }
                    
                    val errorFields = result.errors.map { it.field }.toSet()
                    _formState.value = finalState.copy(
                        touchedFields = finalState.touchedFields + errorFields,
                        errors = result.errors.associate {
                            it.field to it.message
                        }
                    )
                }

                ValidationResult.Valid -> {
                    // 4. Save New Distances
                    finalState.unknownPincodes.forEach { group ->
                         val dist = group.distance.toIntOrNull() ?: 0
                         if (dist > 0) {
                               distanceRepository.setPincodeDistance(firmToSave.pincode, group.pincode, dist)
                         }
                    }

                    _formState.value = FirmFormState()
                    loadFirms()
                    onSuccess()
                }
            }
        }
    }


    val isFormValid: StateFlow<Boolean> =
        formState.map { state ->
                val errors = FirmValidator.validate(state.toFirm(), ValidationLevel.BASE)
                errors.isEmpty() && !state.isLoadingDistances
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )

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

}

sealed class FirmUiEvent {
    data class ShowErrorDialog(val message: String) : FirmUiEvent()
    data object ShowConfirmDialog : FirmUiEvent()
}

