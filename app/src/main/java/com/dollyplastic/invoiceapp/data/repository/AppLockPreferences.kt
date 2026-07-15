package com.dollyplastic.invoiceapp.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import android.content.SharedPreferences

@Singleton
class AppLockPreferences @Inject constructor(
    @ApplicationContext context: Context
)
{
    private val prefs: SharedPreferences
    init {
        prefs = try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // 🔥 CRITICAL FIX
            context.deleteSharedPreferences(FILE_NAME)

            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                FILE_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        }
    }


    companion object {
        private const val FILE_NAME = "app_lock_secure_prefs"
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_BIOMETRIC = "biometric_enabled"
    }

    fun savePinHash(hash: String) {
        prefs.edit().putString(KEY_PIN_HASH, hash).apply()
    }

    fun getPinHash(): String? =
        prefs.getString(KEY_PIN_HASH, null)

    fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply()
    }

    fun isBiometricEnabled(): Boolean =
        prefs.getBoolean(KEY_BIOMETRIC, false)

    fun clear() {
        prefs.edit().clear().apply()
    }
}
