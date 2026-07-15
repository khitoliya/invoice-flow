package com.dollyplastic.invoiceapp.data.repository

import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockRepository @Inject constructor(
    private val secureStorage: AppLockPreferences
)
{

    private var unlockedThisSession = false

    fun isUnlocked(): Boolean = unlockedThisSession

    fun resetUnlock() {
        unlockedThisSession = false
    }

    fun isPinSet(): Boolean =

        secureStorage.getPinHash() != null

    fun setPin(pin: String) {
        secureStorage.savePinHash(hashPin(pin))
        unlockedThisSession = true
    }

    fun verifyPin(pin: String): Boolean {
        val storedHash = secureStorage.getPinHash() ?: return false
        val isValid = storedHash == hashPin(pin)
        if (isValid) unlockedThisSession = true
        return isValid
    }

    fun enableBiometric(enabled: Boolean) {
        secureStorage.setBiometricEnabled(enabled)
    }

    fun isBiometricEnabled(): Boolean =
        secureStorage.isBiometricEnabled()

    fun clearAll() {
        unlockedThisSession = false
        secureStorage.clear()
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(pin.toByteArray())
            .joinToString("") { "%02x".format(it) }
    }
    fun markUnlocked() {
        unlockedThisSession = true
    }
}
