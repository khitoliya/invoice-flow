package com.dollyplastic.invoiceapp.ui.screens.applock


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.repository.AppLockRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AppLockUiState {
    object Idle : AppLockUiState()
    object Verifying : AppLockUiState()
    object Success : AppLockUiState()
    data class Error(val message: String) : AppLockUiState()
}

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val appLockRepository: AppLockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AppLockUiState>(AppLockUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun setPin(pin: String) {
        viewModelScope.launch {
            appLockRepository.setPin(pin)
            _uiState.value = AppLockUiState.Success
        }
    }

    fun verifyPin(pin: String) {
        viewModelScope.launch {
            _uiState.value = AppLockUiState.Verifying
            kotlinx.coroutines.delay(200) // Simulate processing / prevent conflation
            val valid = appLockRepository.verifyPin(pin)
            _uiState.value =
                if (valid) AppLockUiState.Success
                else AppLockUiState.Error("Incorrect PIN")
        }
    }
    
    fun resetState() {
        _uiState.value = AppLockUiState.Idle
    }
    fun enableBiometricIfAvailable() {
        viewModelScope.launch {
            appLockRepository.enableBiometric(true)
        }
    }

    fun markUnlocked() {
        viewModelScope.launch {
            appLockRepository.markUnlocked()
            _uiState.value = AppLockUiState.Success
        }
    }

    fun isBiometricEnabled(): Boolean =
        appLockRepository.isBiometricEnabled()

    fun isPinSet(): Boolean {
        return appLockRepository.isPinSet()
    }

}
