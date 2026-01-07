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
import javax.inject.Inject
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Validation.PartyValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import com.dollyplastic.invoiceapp.domain.config.StateConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class PartyViewModel @Inject constructor(
    private val repository: PartyRepository,
    private val addPartyUseCase: AddPartyUseCase
) : ViewModel() {

    /* ---------- LIST ---------- */

    private val _parties = MutableStateFlow<List<Party>>(emptyList())
    val parties = _parties.asStateFlow()

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
            .map { it.errors.isEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5_000),
                false
            )


    init {
        loadParties()
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
                is Result.Success ->
                    _formState.value = PartyFormState.fromParty(r.data)
                else -> Unit
            }
        }
    }

    fun onFieldChange(field: String, value: String) {
        var updated = _formState.value.update(field, value)

        // auto derive stateCode from GSTIN
        if (field == "gstin" && value.length >= 2) {
            updated = updated.copy(stateCode = value.take(2))
        }

        val errors = PartyValidator
            .validate(updated.toParty(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }

        _formState.value = updated.copy(errors = errors)
    }
    fun onGstinChange(value: String) {
        var updated = _formState.value.update("gstin", value.uppercase())

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

        val errors = PartyValidator
            .validate(updated.toParty(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }

        _formState.value = updated.copy(errors = errors)
    }
    fun requestSaveConfirmation() {
        viewModelScope.launch {
            _uiEvent.emit(PartyUiEvent.ShowConfirmDialog)
        }
    }



    fun saveParty(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val result = addPartyUseCase.execute(_formState.value.toParty())

            when (result) {
                is ValidationResult.Valid -> {
                    _formState.value = PartyFormState()
                    loadParties()
                    onSuccess()
                }
                is ValidationResult.Invalid -> {
                    val dup = result.errors.firstOrNull { it.field == "gstin" }
                    if (dup != null) {
                        _uiEvent.emit(PartyUiEvent.ShowErrorDialog(dup.message))

                    } else {
                        _formState.value = _formState.value.copy(
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
