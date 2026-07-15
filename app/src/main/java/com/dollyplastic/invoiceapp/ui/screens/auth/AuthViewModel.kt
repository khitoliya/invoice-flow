package com.dollyplastic.invoiceapp.ui.screens.auth


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.repository.AppLockRepository
import com.dollyplastic.invoiceapp.data.repository.AuthRepository

import com.dollyplastic.invoiceapp.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Error(val message: String) : AuthUiState()
    object Success : AuthUiState()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appLockRepository: AppLockRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun loginWithEmail(email: String, password: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.loginWithEmail(email, password)

                if (!authRepository.isUserAuthorized()) {
                    authRepository.logout()
                    _uiState.value = AuthUiState.Error(
                        "This account is not authorized to use this app."
                    )
                    return@launch
                }

                routeAfterLogin(onSuccess)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    "Incorrect email or password"
                )
            }
        }
    }

    fun loginWithGoogle(idToken: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            try {
                authRepository.loginWithGoogle(idToken)

                if (!authRepository.isUserAuthorized()) {
                    authRepository.logout()
                    _uiState.value = AuthUiState.Error(
                        "This account is not authorized to use this app."
                    )
                    return@launch
                }

                routeAfterLogin(onSuccess)
            } catch (e: Exception) {
                _uiState.value = AuthUiState.Error(
                    "Google sign-in failed"
                )
            }
        }
    }

    fun sendPasswordReset(email: String) {
        viewModelScope.launch {
            try {
                authRepository.sendPasswordReset(email)
                _uiState.value = AuthUiState.Error(
                    "If this email is authorized, a reset link has been sent."
                )
            } catch (_: Exception) {
                // Always show same message (security)
                _uiState.value = AuthUiState.Error(
                    "If this email is authorized, a reset link has been sent."
                )
            }
        }
    }

    private suspend fun routeAfterLogin(onSuccess: (String) -> Unit) {
        val destination =
            if (appLockRepository.isPinSet()) {
                Route.AppLockGraph.route
            } else {
                Route.PinSetup.route
            }

        _uiState.value = AuthUiState.Success
        onSuccess(destination)
    }
}
