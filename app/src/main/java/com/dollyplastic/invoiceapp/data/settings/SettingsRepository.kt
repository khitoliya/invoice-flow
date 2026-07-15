package com.dollyplastic.invoiceapp.data.settings

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WA_NAME = "whatsapp_contact_name"
        private const val KEY_WA_NUMBER = "whatsapp_contact_number"
        
        // Drive Backup Keys
        private const val KEY_DRIVE_ENABLED = "drive_backup_enabled"
        private const val KEY_DRIVE_NETWORK = "drive_network_type" // "WIFI" or "ANY"
        private const val KEY_DRIVE_FOLDER_URL = "drive_folder_url"
        private const val KEY_DRIVE_EMAIL = "drive_connected_email"
        private const val KEY_LAST_BACKUP_TIME = "drive_last_backup_time"
    }

    fun getWhatsAppContact(): Pair<String, String>? {
        val name = prefs.getString(KEY_WA_NAME, null)
        val number = prefs.getString(KEY_WA_NUMBER, null)
        return if (name != null && number != null) {
            name to number
        } else {
            null
        }
    }

    fun saveWhatsAppContact(name: String, number: String) {
        prefs.edit()
            .putString(KEY_WA_NAME, name)
            .putString(KEY_WA_NUMBER, number)
            .apply()
    }

    fun clearWhatsAppContact() {
        prefs.edit()
            .remove(KEY_WA_NAME)
            .remove(KEY_WA_NUMBER)
            .apply()
    }

    // --- Drive Backup Settings ---
    
    fun isDriveBackupEnabled(): Boolean = prefs.getBoolean(KEY_DRIVE_ENABLED, false)
    fun setDriveBackupEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DRIVE_ENABLED, enabled).apply()
    }
    
    fun getDriveNetworkType(): String = prefs.getString(KEY_DRIVE_NETWORK, "WIFI") ?: "WIFI"
    fun setDriveNetworkType(type: String) {
        prefs.edit().putString(KEY_DRIVE_NETWORK, type).apply()
    }
    
    fun getDriveFolderUrl(): String? = prefs.getString(KEY_DRIVE_FOLDER_URL, null)
    fun setDriveFolderUrl(url: String?) {
        if (url.isNullOrBlank()) {
            prefs.edit().remove(KEY_DRIVE_FOLDER_URL).apply()
        } else {
            prefs.edit().putString(KEY_DRIVE_FOLDER_URL, url.trim()).apply()
        }
    }
    
    fun extractDriveFolderId(): String? {
        val url = getDriveFolderUrl() ?: return null
        return try {
            if (url.contains("id=")) {
                url.substringAfter("id=").substringBefore("&")
            } else if (url.contains("/folders/")) {
                url.substringAfter("/folders/").substringBefore("?").substringBefore("/")
            } else {
                url // Fallback, assume they pasted the ID directly
            }
        } catch (e: Exception) {
            null
        }
    }
    
    fun getDriveConnectedEmail(): String? = prefs.getString(KEY_DRIVE_EMAIL, null)
    fun setDriveConnectedEmail(email: String?) {
        if (email.isNullOrBlank()) {
            prefs.edit().remove(KEY_DRIVE_EMAIL).apply()
        } else {
            prefs.edit().putString(KEY_DRIVE_EMAIL, email).apply()
        }
    }
    
    fun getLastBackupTimestamp(): Long = prefs.getLong(KEY_LAST_BACKUP_TIME, 0L)
    fun setLastBackupTimestamp(time: Long) {
        prefs.edit().putLong(KEY_LAST_BACKUP_TIME, time).apply()
    }
}
