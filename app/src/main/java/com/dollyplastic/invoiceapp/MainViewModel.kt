package com.dollyplastic.invoiceapp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.repository.AppLockRepository
import com.dollyplastic.invoiceapp.data.repository.AuthRepository
import com.dollyplastic.invoiceapp.ui.navigation.Route
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val appLockRepository: AppLockRepository
) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _startDestination = MutableStateFlow(Route.Auth.route)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            resolveStartDestination()
        }
    }

    private suspend fun resolveStartDestination() {
        delay(2000) // Keep splash screen for 2 seconds
        val user = authRepository.currentUser

        if (user == null) {
            _startDestination.value = Route.Auth.route
        } else {
            val authorized = authRepository.isUserAuthorized()

            if (!authorized) {
                authRepository.logout()
                _startDestination.value = Route.Auth.route
            } else if (!appLockRepository.isPinSet()) {
                // PIN NOT SET → FORCE PIN SETUP
                _startDestination.value = Route.AppLockGraph.route
            } else if (!appLockRepository.isUnlocked()) {
                // PIN SET BUT LOCKED → VERIFY PIN / BIOMETRIC
                _startDestination.value = Route.AppLockGraph.route
            } else {
                _startDestination.value = Route.Main.route
            }
        }

        _isLoading.value = false
    }
}
