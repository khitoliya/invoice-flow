package com.dollyplastic.invoiceapp.ui.screens.processing

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dollyplastic.invoiceapp.data.credentials.Credential
import com.dollyplastic.invoiceapp.data.credentials.CredentialRepository
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Workflow.InvoiceWorkflowManager
import com.dollyplastic.invoiceapp.domain.Workflow.WorkflowResult
import com.dollyplastic.invoiceapp.ui.common.deletion.InvoiceDeletionHelper
import com.dollyplastic.invoiceapp.ui.screens.processing.InvoiceProcessingUiEvent.*
import com.dollyplastic.invoiceapp.ui.screens.processing.components.InvoiceTimelineMapper
import com.dollyplastic.invoiceapp.ui.screens.processing.components.TimelineActionType
import com.dollyplastic.invoiceapp.ui.screens.processing.components.TimelineStep
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class ProcessingState(
    val invoice: Invoice? = null,
    val isLoading: Boolean = true,
    val message: String = "Initializing...",
    val credentials: List<Credential> = emptyList(),

    val timeline: List<TimelineStep> = emptyList(),

    val portalUrl: String? = null,
    val lastDownloadedFile: File? = null,

    val isComplianceLocked: Boolean = false,

    // Post-generation compliance fields
    val showInlineTransportForm: Boolean = false,
    val pendingComplianceAction: String? = null, // "EWAY" or "EINVOICE"
    val draftTransportDetails: com.dollyplastic.invoiceapp.data.models.TransportDetails = com.dollyplastic.invoiceapp.data.models.TransportDetails(),
    val transportErrors: Map<String, String> = emptyMap(),
    val isEWayBillAllowed: Boolean = false,
    val isEInvoiceAllowed: Boolean = false
)




sealed interface InvoiceProcessingUiEvent {
    data class NavigateToPortal(val url: String, val storageRef: InvoiceStorageRef, val credentialsJson: String, val isCancellation: Boolean = false, val mode: String = "EWAY") : InvoiceProcessingUiEvent
    object NavigateBack : InvoiceProcessingUiEvent
    data class ViewPdf(val invoice: Invoice) : InvoiceProcessingUiEvent
    data class SharePdf(val file: File, val preferredContact: Pair<String, String>?) : InvoiceProcessingUiEvent
    data class NavigateToEdit(val invoiceId: String) : InvoiceProcessingUiEvent
    data class ShowError(val message: String) : InvoiceProcessingUiEvent
    object InvoiceDeleted : InvoiceProcessingUiEvent
}

@HiltViewModel
class InvoiceProcessingViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val workflowManager: InvoiceWorkflowManager,
    private val repository: InvoiceRepository,
    private val credentialRepository: CredentialRepository,
    private val settingsRepository: com.dollyplastic.invoiceapp.data.settings.SettingsRepository,
    val deletionHelper: InvoiceDeletionHelper,
    private val timelineMapper: InvoiceTimelineMapper,
    private val savedStateHandle: androidx.lifecycle.SavedStateHandle
) : ViewModel() {

    private val _state = MutableStateFlow(ProcessingState())
    val state = _state.asStateFlow()
    
    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<InvoiceProcessingUiEvent>()
    val events = _events.asSharedFlow()
    

    init {
        // debug: Print keys
        // android.util.Log.d(TAG, "Keys: " + savedStateHandle.keys().toString())

        viewModelScope.launch {
            savedStateHandle.getStateFlow<String?>("download_result", null)
                .collect { path ->
                    android.util.Log.d(TAG, "Result emission: $path")
                    if (path != null) {
                        onResultFileDownloaded(File(path))
                        // Clear
                        savedStateHandle["download_result"] = null
                    }
                }
        }
    }
    
    // ... rest of class logic ...
    
    private var lastKnownStatus: InvoiceStatus? = null

    private val TAG = "InvoiceWorkflow"
    /* ---------------- LOAD ---------------- */

    private var invoiceCollectionJob: kotlinx.coroutines.Job? = null

    /* ---------------- LOAD ---------------- */

    fun loadInvoice(invoiceId: String, autoStart: Boolean) {
        if (invoiceCollectionJob?.isActive == true && _state.value.invoice?.invoiceId == invoiceId) {
             android.util.Log.d(TAG, "[ViewModel] Already collecting invoice $invoiceId. Skipping re-load.")
             return
        }
        
        invoiceCollectionJob?.cancel()
        invoiceCollectionJob = viewModelScope.launch {
            repository.observeInvoice(invoiceId).collect { invoice ->
                if (invoice == null) {
                    android.util.Log.d(TAG, "[ViewModel] Invoice $invoiceId is null (Deleted/Archived). Stopping collection.")
                    return@collect
                }

                val creds = credentialRepository.getAll()
                val sortedCreds = creds.sortedByDescending {
                    it.firmId == invoice.firm.firmId
                }
                
                // Track state changes to avoid unnecessary logging
                if (lastKnownStatus != invoice.status) {
                    android.util.Log.d(TAG, "[ViewModel] Invoice Status Changed: ${lastKnownStatus} -> ${invoice.status}")
                    lastKnownStatus = invoice.status
                }
                
                // Intent-Based Auto-Start Logic
                if (autoStart && invoice.status == InvoiceStatus.DRAFT) {
                    android.util.Log.d(TAG, "[ViewModel] Triggering Auto-Start for draft invoice")
                    startWorkflow(invoiceId)
                } else if (invoice.status == InvoiceStatus.GENERATING_JSON || 
                           (invoice.status == InvoiceStatus.WAITING_FOR_UPLOAD && _state.value.portalUrl == null)) {
                     // Check if we already tried to auto-resume recently to avoid loops?
                     // actually, startWorkflow is safe to call multiple times as manager handles it.
                     android.util.Log.d(TAG, "[ViewModel] Resuming workflow for ${invoice.status}")
                     startWorkflow(invoiceId)
                }

                // Determine limits for post-generation
                var isEWayBillAllowed = false
                var isEInvoiceAllowed = false
                if (invoice.status == InvoiceStatus.COMPLETED && !invoice.isCashSale) {
                    val days = java.time.temporal.ChronoUnit.DAYS.between(
                        com.dollyplastic.invoiceapp.domain.Utils.DateUtils.parse(invoice.invoiceDate),
                        com.dollyplastic.invoiceapp.domain.Utils.DateUtils.parse(com.dollyplastic.invoiceapp.domain.Utils.DateUtils.today())
                    ).toInt()

                    if (days in 0..180) isEWayBillAllowed = true
                    if (days in 0..30) isEInvoiceAllowed = true
                }

                _state.update {
                    it.copy(
                        invoice = invoice,
                        credentials = sortedCreds,
                        isComplianceLocked =
                            invoice.status == InvoiceStatus.COMPLETED &&
                                    (invoice.generateEInvoice || invoice.generateEWayBill),
                        timeline = timelineMapper.map(invoice),
                        isLoading = false,
                        isEWayBillAllowed = isEWayBillAllowed,
                        isEInvoiceAllowed = isEInvoiceAllowed
                    )
                }
            }
        }
    }

    private fun currentStorageRef(): InvoiceStorageRef? {
        val invoice = _state.value.invoice ?: return null
        return InvoiceStorageRef(
            firmName = com.dollyplastic.invoiceapp.data.repository.InvoiceStorage.getFirmIdentifier(invoice.firm),
            financialYear = invoice.financialYear,
            invoiceNumber = invoice.invoiceNumber
        )
    }

    /* ---------------- POST-GENERATION COMPLIANCE ---------------- */

    fun initiateComplianceAddition(type: String) {
        val invoice = _state.value.invoice ?: return
        
        var details = invoice.transportDetails
        
        // If they had buyer pickup, softly upgrade it to own vehicle to save clicks
        if (details.deliveryType == com.dollyplastic.invoiceapp.data.models.DeliveryType.BUYER_PICKUP) {
             details = details.copy(deliveryType = com.dollyplastic.invoiceapp.data.models.DeliveryType.OWN_VEHICLE)
        }

        _state.update { 
            it.copy(
                showInlineTransportForm = true,
                pendingComplianceAction = type,
                draftTransportDetails = details,
                transportErrors = emptyMap()
            )
        }
    }

    fun updateDraftTransport(details: com.dollyplastic.invoiceapp.data.models.TransportDetails) {
         _state.update { it.copy(draftTransportDetails = details) }
    }
    
    fun onDraftTransportFieldBlur(field: String) {
         // Optionally validate single field
         val inv = _state.value.invoice ?: return
         val type = _state.value.pendingComplianceAction ?: return
         // we won't aggressively clear errors for now, rely on Proceed validation.
    }
    
    fun cancelComplianceAddition() {
         _state.update { 
            it.copy(
                showInlineTransportForm = false,
                pendingComplianceAction = null,
                transportErrors = emptyMap()
            )
        }
    }

    fun confirmComplianceAddition() {
         val inv = _state.value.invoice ?: return
         val currentType = _state.value.pendingComplianceAction ?: return
         
         // Build the future invoice object
         val updatedInvoice = inv.copy(
             transportDetails = _state.value.draftTransportDetails,
             generateEWayBill = if (currentType == "EWAY") true else inv.generateEWayBill,
             generateEInvoice = if (currentType == "EINVOICE") true else inv.generateEInvoice,
             status = InvoiceStatus.DRAFT // Reset to draft to trigger payload generation
         )

         // Validate using the whole InvoiceValidator
         val level = if (currentType == "EWAY") com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel.E_WAY else com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel.E_INVOICE
         val result = com.dollyplastic.invoiceapp.domain.Validation.InvoiceValidator.validate(updatedInvoice, level)

         if (result is com.dollyplastic.invoiceapp.domain.Validation.ValidationResult.Invalid) {
             val errors = result.errors
             val transportErrMap = errors.filter { it.section == "Transport" }.associate { it.field to it.message }
             
             if (transportErrMap.isNotEmpty()) {
                  _state.update { it.copy(transportErrors = transportErrMap) }
             } else {
                  // Non-transport error (should be rare on an already completed invoice)
                  viewModelScope.launch {
                      _events.emit(InvoiceProcessingUiEvent.ShowError("Validation Failed. Please check all details."))
                  }
             }
             return
         }

         // It's valid. Update the repository and restart workflow.
         _state.update { currentState -> 
             currentState.copy(showInlineTransportForm = false, pendingComplianceAction = null) 
         }
         
         viewModelScope.launch {
             repository.updateInvoice(updatedInvoice)
             // The collector will observe the DRAFT status and auto call startWorkflow
         }
    }


    /* ---------------- WORKFLOW ---------------- */

    /* ---------------- WORKFLOW ---------------- */

    // Helper to determine URL and Creds
    private fun preparePortalNavigation(forceUrl: String? = null): InvoiceProcessingUiEvent.NavigateToPortal? {
         // Determine URL: Cache -> E-Invoice -> E-Way Bill -> Default
        val inv = state.value.invoice ?: return null
         
        val url = forceUrl ?: state.value.portalUrl ?: com.dollyplastic.invoiceapp.data.utils.PortalUtils.getPortalUrl(inv)
        
        // Determine Mode based on URL & Flags
        val mode = com.dollyplastic.invoiceapp.data.utils.PortalUtils.getPortalMode(inv, url)

        
        // Filter Credentials
        val firmId = inv.firm.firmId
        
        val firmCreds = state.value.credentials.filter { it.firmId == firmId }
        
        // Secondary Filter (Portal Link)
        val filteredCreds = if (url.contains("eway", true)) {
            firmCreds.filter { 
                it.url.contains("eway", true) || it.name.contains("eway", true)
            }
        } else if (url.contains("einvoice", true)) {
            firmCreds.filter { 
                 it.url.contains("einvoice", true) || it.name.contains("einvoice", true)
            }
        } else {
            firmCreds
        }
        
        val finalCreds = if (filteredCreds.isNotEmpty()) filteredCreds else firmCreds
        val json = com.google.gson.Gson().toJson(finalCreds)
        
        return InvoiceProcessingUiEvent.NavigateToPortal(
            url,
            currentStorageRef()!!,
            json,
            isCancellation = false, // Default
            mode = mode
        )
    }

    private fun startWorkflow(invoiceId: String) {
        android.util.Log.d(TAG, "[ViewModel] startWorkflow called for $invoiceId")
        viewModelScope.launch {
            val context = appContext // inject Application context
            when (val result = workflowManager.startProcessing(invoiceId, context)) {
                is WorkflowResult.JsonReady -> {
                    _state.update { it.copy(portalUrl = result.portalUrl) }
                    // Flow will update status automatically if manager changed it
                }
                is WorkflowResult.ProcessingStarted,
                is WorkflowResult.Completed,
                is WorkflowResult.Error -> {
                    // Flow updates automatically
                }
            }
        }
    }
    
    fun openPortalForCancellation() {
         viewModelScope.launch {
             val event = preparePortalNavigation()
             if (event != null) {
                 _events.emit(event.copy(isCancellation = true))
             }
         }
    }

    fun onResultFileDownloaded(file: File) {
        val invoiceId = _state.value.invoice?.invoiceId ?: return
        android.util.Log.d(TAG, "[ViewModel] onResultFileDownloaded: ${file.name} for Invoice: $invoiceId")

        _state.update { it.copy(lastDownloadedFile = file) }

        viewModelScope.launch {
            workflowManager.onResultDownloaded(invoiceId, file, appContext)
            // Flow updates automatically
        }
    }

    fun handleTimelineAction(action: TimelineActionType) {
        android.util.Log.d(TAG, "[ViewModel] handleTimelineAction: $action")
        viewModelScope.launch {
            when (action) {
                TimelineActionType.RETRY_JSON -> {
                    _state.value.invoice?.invoiceId?.let {
                        startWorkflow(it)
                    }
                }

                TimelineActionType.OPEN_PORTAL -> {
                     val event = preparePortalNavigation()
                     if (event != null) {
                         _events.emit(event)
                     }
                }

                TimelineActionType.RETRY_PARSING -> {
                    state.value.lastDownloadedFile?.let {
                        onResultFileDownloaded(it)
                    }
                }

                TimelineActionType.REDOWNLOAD_RESULT -> {
                    handleTimelineAction(TimelineActionType.OPEN_PORTAL)
                }

                TimelineActionType.VIEW_PDF -> {
                    state.value.invoice?.let {
                        _events.emit(InvoiceProcessingUiEvent.ViewPdf(it))
                    }
                }

                TimelineActionType.RETRY_UPLOAD -> {
                    state.value.portalUrl?.let { url ->
                        // Filter Credentials logic (Duplicated for consistency)
                        val inv = state.value.invoice
                        val firmId = inv?.firm?.firmId
                        val firmCreds = state.value.credentials.filter { it.firmId == firmId }
                        
                        val filteredCreds = if (url.contains("eway", true)) {
                            firmCreds.filter { 
                                it.url.contains("eway", true) || it.name.contains("eway", true)
                            }
                        } else if (url.contains("einvoice", true)) {
                            firmCreds.filter { 
                                 it.url.contains("einvoice", true) || it.name.contains("einvoice", true)
                            }
                        } else {
                            firmCreds
                        }
                         val finalCreds = if (filteredCreds.isNotEmpty()) filteredCreds else firmCreds
                         val json = com.google.gson.Gson().toJson(finalCreds)
                    
                        _events.emit(
                            InvoiceProcessingUiEvent.NavigateToPortal(
                                url,
                                currentStorageRef()!!,
                                json
                            )
                        )
                    }
                }

                TimelineActionType.SHARE_PDF -> {
                    // 1. Try Shared Invoice PDF First
                    val inv = state.value.invoice
                    if (inv != null) {
                        val pdfFile = InvoiceStorage.getFinalPdfFile(inv.firm, inv)
                        if (pdfFile.exists()) {
                             val contact = settingsRepository.getWhatsAppContact()
                             _events.emit(InvoiceProcessingUiEvent.SharePdf(pdfFile, contact))
                        } else {
                            // 2. Fallback to last downloaded ONLY IF it exists
                             val fallback = state.value.lastDownloadedFile
                             if (fallback != null && fallback.exists()) {
                                 val contact = settingsRepository.getWhatsAppContact()
                                 _events.emit(InvoiceProcessingUiEvent.SharePdf(fallback, contact))
                             } else {
                                _events.emit(InvoiceProcessingUiEvent.ShowError("PDF not found. Please click 'View PDF' to generate it."))
                             }
                        }
                    }
                }



                TimelineActionType.EDIT_INVOICE -> {
                    state.value.invoice?.invoiceId?.let { id ->
                        _events.emit(InvoiceProcessingUiEvent.NavigateToEdit(id))
                    }
                }
            }
        }
    }


    /* ---------------- DELETION ---------------- */

    fun onDeleteClicked() {
        val inv = _state.value.invoice ?: return
        viewModelScope.launch {
            deletionHelper.onRequestDeletion(inv)
        }
    }

    fun dismissDeleteDialog() {
        deletionHelper.dismiss()
    }

    fun confirmHardDelete(invoice: Invoice) {
        viewModelScope.launch {
            val result = deletionHelper.onConfirmHardDelete(invoice)
            if (result is Result.Success) {
                _events.emit(InvoiceProcessingUiEvent.InvoiceDeleted)
            } else {
                _state.update { it.copy(message = "Deletion Failed") }
            }
        }
    }

    fun confirmCancellation(invoice: Invoice, remark: String) {
        viewModelScope.launch {
            val result = deletionHelper.onConfirmArchive(invoice, remark)
             if (result is Result.Success) {
                 _events.emit(InvoiceProcessingUiEvent.InvoiceDeleted)
            } else {
                 _state.update { it.copy(message = "Cancellation Failed") }
            }
        }
    }
}
