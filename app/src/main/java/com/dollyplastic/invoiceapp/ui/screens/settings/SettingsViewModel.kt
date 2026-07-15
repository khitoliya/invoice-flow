package com.dollyplastic.invoiceapp.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.credentials.Credential
import com.dollyplastic.invoiceapp.data.credentials.CredentialRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.repository.FirmRepository

data class SettingsState(
    val credentials: List<Credential> = emptyList(),
    val firms: List<Firm> = emptyList(),
    val isDriveBackupEnabled: Boolean = false,
    val driveFolderUrl: String = "",
    val driveNetworkType: String = "WIFI",
    val driveConnectedEmail: String? = null,
    val lastBackupTime: Long = 0L
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: CredentialRepository,
    private val firmRepository: FirmRepository,
    private val settingsRepository: com.dollyplastic.invoiceapp.data.settings.SettingsRepository
) : ViewModel() {

    // WhatsApp Contact State
    private val _waContact = MutableStateFlow<Pair<String, String>?>(null)
    val waContact = _waContact.asStateFlow()

    private val _state = MutableStateFlow(SettingsState())
    val state = _state.asStateFlow()

    init {
        loadCredentials()
        loadWhatsAppContact()
        loadDriveSettings()
    }
    
    private fun loadDriveSettings() {
        _state.update { 
            it.copy(
                isDriveBackupEnabled = settingsRepository.isDriveBackupEnabled(),
                driveFolderUrl = settingsRepository.getDriveFolderUrl() ?: "",
                driveNetworkType = settingsRepository.getDriveNetworkType(),
                driveConnectedEmail = settingsRepository.getDriveConnectedEmail(),
                lastBackupTime = settingsRepository.getLastBackupTimestamp()
            )
        }
    }
    
    fun setDriveBackupEnabled(enabled: Boolean) {
        settingsRepository.setDriveBackupEnabled(enabled)
        loadDriveSettings()
    }
    
    fun setDriveFolderUrl(url: String) {
        settingsRepository.setDriveFolderUrl(url)
        loadDriveSettings()
    }
    
    fun setDriveNetworkType(type: String) {
        settingsRepository.setDriveNetworkType(type)
        loadDriveSettings()
    }
    
    fun setDriveConnectedEmail(email: String?) {
        settingsRepository.setDriveConnectedEmail(email)
        loadDriveSettings()
    }

    private fun loadWhatsAppContact() {
        _waContact.value = settingsRepository.getWhatsAppContact()
    }

    fun setWhatsAppContact(name: String, number: String) {
        settingsRepository.saveWhatsAppContact(name, number)
        loadWhatsAppContact()
    }

    fun clearWhatsAppContact() {
        settingsRepository.clearWhatsAppContact()
        loadWhatsAppContact()
    }

    private fun loadCredentials() {
        _state.update { it.copy(credentials = repository.getAll()) }
    }
    
    // Load firms for dropdown
    fun loadFirms() {
        viewModelScope.launch {
             val result = firmRepository.getAllFirms()
             if (result is com.dollyplastic.invoiceapp.data.utils.Result.Success) {
                 _state.update { it.copy(firms = result.data) }
             }
        }
    }

    fun addCredential(name: String, username: String, pass: String, url: String, id: String? = null, firmId: String? = null) {
        if (name.isBlank() || username.isBlank() || pass.isBlank()) return
        
        if (id != null) {
            // Edit existing
            val cred = Credential(
                id = id,
                name = name,
                username = username,
                password = pass,
                url = url.ifBlank { "https://ewaybillgst.gov.in" },
                firmId = firmId
            )
            repository.update(cred)
        } else {
            // Add new
            val cred = Credential(
                name = name,
                username = username,
                password = pass,
                url = url.ifBlank { "https://ewaybillgst.gov.in" },
                firmId = firmId
            )
            repository.add(cred)
        }
        loadCredentials()
    }

    fun deleteCredential(id: String) {
        repository.delete(id)
        loadCredentials()
    }
}
