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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FirmViewModel @Inject constructor(
    private val repository: FirmRepository,
    private val addFirmUseCase: AddFirmUseCase
) : ViewModel() {


    private val _uiEvent = MutableSharedFlow<FirmUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    /* ---------------- LIST STATE ---------------- */

    private val _firms = MutableStateFlow<List<Firm>>(emptyList())
    val firms = _firms.asStateFlow()

    /* ---------------- FORM STATE ---------------- */

    private val _formState = MutableStateFlow(FirmFormState())
    val formState = _formState.asStateFlow()

    init {
        loadFirms()
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
                is Result.Success ->
                    _formState.value = FirmFormState.fromFirm(result.data)
                is Result.Error -> Unit
            }
        }
    }

    fun onFieldChange(field: String, value: String) {
        var updated = _formState.value.update(field, value)



        val firm = updated.toFirm()

        val errors = FirmValidator
            .validate(firm, ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }

        _formState.value = updated.copy(errors = errors)
    }

    fun requestSaveConfirmation() {
        viewModelScope.launch {
            _uiEvent.emit(FirmUiEvent.ShowConfirmDialog)
        }
    }



    fun saveFirm(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val firm = _formState.value.toFirm()
            val result = addFirmUseCase.execute(firm)

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

                    _formState.value = _formState.value.copy(
                        errors = result.errors.associate {
                            it.field to it.message
                        }
                    )
                }

                ValidationResult.Valid -> {
                    _formState.value = FirmFormState()
                    loadFirms()
                    onSuccess()
                }
            }
        }
    }


    val isFormValid: StateFlow<Boolean> =
        formState.map { it.errors.isEmpty() }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(5000),
                false
            )

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

        val errors = FirmValidator
            .validate(updated.toFirm(), ValidationLevel.BASE)
            .associateBy { it.field }
            .mapValues { it.value.message }


        _formState.value = updated.copy(errors = errors)
    }

}

sealed class FirmUiEvent {
    data class ShowErrorDialog(val message: String) : FirmUiEvent()
    data object ShowConfirmDialog : FirmUiEvent()
}

