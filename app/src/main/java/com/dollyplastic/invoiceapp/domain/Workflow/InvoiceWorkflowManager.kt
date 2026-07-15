package com.dollyplastic.invoiceapp.domain.Workflow

import android.content.Context
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Compliance.ComplianceResult
import com.dollyplastic.invoiceapp.domain.Compliance.ComplianceRunner
import com.dollyplastic.invoiceapp.pdf.InvoicePdfGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceWorkflowManager @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val repository: InvoiceRepository,
    private val complianceRunner: ComplianceRunner,
    private val statusUpdater: InvoiceStatusUpdater,
) {
    private val TAG = "InvoiceWorkflow"

    suspend fun startProcessing(invoiceId: String, context: Context): WorkflowResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        android.util.Log.d(TAG, "[OPTIMIZED_FLOW] [Manager] startProcessing: $invoiceId")
        val invoiceResult = repository.getInvoice(invoiceId)
        val invoice = when (invoiceResult) {
            is Result.Success -> invoiceResult.data
            is Result.Error -> {
                repository.updateInvoiceStatus(
                    invoiceId,
                    InvoiceStatus.VALIDATION_FAILED,
                )
                return@withContext WorkflowResult.Error("Failed to get Invoice From Repository")
            }
        }

        // Track current status locally to avoid stale state issues
        var currentStatus = invoice.status

        /* ---------------- SIMPLE INVOICE (No Compliance) ---------------- */
        if (!invoice.generateEInvoice && !invoice.generateEWayBill) {
            android.util.Log.d(TAG, "[Manager] Simple Invoice (No compliance). generating PDF and completing.")

            // 1. Generate PDF
            InvoicePdfGenerator.generateForAndroid(context, invoice)

            // 2. Mark Completed
            statusUpdater.transition(
                invoiceId = invoiceId,
                from = currentStatus,
                to = InvoiceStatus.COMPLETED
            )
            return@withContext WorkflowResult.Completed
        }

        /* ---------------- COMPLIANCE FLOW ---------------- */

        // 1. Check if we are merely resuming (File exists)
        if (currentStatus == InvoiceStatus.WAITING_FOR_UPLOAD) {
             val fileStart = if(invoice.generateEInvoice) "Payload_EInvoice" else "Payload_EWayBill"
             val tempDir = com.dollyplastic.invoiceapp.data.repository.InvoiceStorage.getTemporaryDirectory(invoice.firm, invoice.invoiceNumber)
             val jsonFile = File(tempDir, "$fileStart.json")
             
             if (jsonFile.exists()) {
                 android.util.Log.d(TAG, "[OPTIMIZED_FLOW] [Manager] Resuming from WAITING_FOR_UPLOAD, JSON ready.")
                 val url = com.dollyplastic.invoiceapp.data.utils.PortalUtils.getPortalUrl(invoice)
                 return@withContext WorkflowResult.JsonReady(jsonFile, url)
             } else {
                  android.util.Log.w(TAG, "[OPTIMIZED_FLOW] [Manager] JSON missing. Will regenerate via Worker.")
             }
        }

        if (currentStatus != InvoiceStatus.GENERATING_JSON && 
            currentStatus != InvoiceStatus.PROCESSING_RESULT && 
            currentStatus != InvoiceStatus.COMPLETED) {
            
            statusUpdater.transition(
                invoiceId = invoiceId,
                from = currentStatus,
                to = InvoiceStatus.GENERATING_JSON
            )
            currentStatus = InvoiceStatus.GENERATING_JSON
        } 


        try {
            val result = complianceRunner.run(context, invoice)
            android.util.Log.d(TAG, "[Manager] ComplianceRunner Result: $result")

            when (result) {
                is ComplianceResult.PdfOnly -> {
                    // Should be handled by top check, but safe fallback
                    statusUpdater.transition(
                        invoiceId = invoiceId,
                        from = currentStatus,
                        to = InvoiceStatus.COMPLETED
                    )
                    WorkflowResult.Completed
                }

                is ComplianceResult.ReadyForPortal -> {
                    // Only transition if we are NOT already there (Race condition safety)
                    if (currentStatus != InvoiceStatus.WAITING_FOR_UPLOAD) {
                        statusUpdater.transition(
                            invoiceId = invoiceId,
                            from = currentStatus,
                            to = InvoiceStatus.WAITING_FOR_UPLOAD
                        )
                        currentStatus = InvoiceStatus.WAITING_FOR_UPLOAD
                    }
                    
                    WorkflowResult.JsonReady(
                        file = result.jsonFile,
                        portalUrl = result.portalUrl
                    )
                }

                is ComplianceResult.Error -> {
                    statusUpdater.transition(
                        invoiceId = invoiceId,
                        from = currentStatus,
                        to = InvoiceStatus.JSON_GENERATION_FAILED,
                        reason = result.message
                    )
                    
                    WorkflowResult.Error(result.message)
                }
            }
        } catch (e: Exception) {
            // Only mark failed if we were actually generating JSON
            if (currentStatus == InvoiceStatus.GENERATING_JSON) {
                statusUpdater.transition(
                    invoiceId = invoiceId,
                    from = currentStatus,
                    to = InvoiceStatus.JSON_GENERATION_FAILED,
                    reason = e.message
                )
            } else {
                 android.util.Log.e(TAG, "[Manager] Exception occurred but status was $currentStatus (not GENERATING_JSON), suppressing transition.", e)
            }
            WorkflowResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun onResultDownloaded(
        invoiceId: String,
        file: File,
        context: Context
    ): WorkflowResult = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        android.util.Log.d(TAG, "[OPTIMIZED_FLOW] [Manager] onResultDownloaded: ${file.absolutePath}")

        // 1️⃣ Load invoice FIRST to verify
        val invoiceResult = repository.getInvoice(invoiceId)
        val invoice = when (invoiceResult) {
            is Result.Success -> invoiceResult.data
            is Result.Error -> {
                return@withContext WorkflowResult.Error("Failed to load invoice from repository")
            }
        }

        // 2️⃣ Transition → PROCESSING_RESULT (Immediate UI Feedback)
        statusUpdater.transition(
            invoiceId = invoiceId,
            from = invoice.status,
            to = InvoiceStatus.PROCESSING_RESULT
        )

        // 3️⃣ Enqueue Background Worker
        android.util.Log.d(TAG, "[OPTIMIZED_FLOW] [Manager] Enqueuing Background Worker for Result Processing.")
        
        val workRequest = androidx.work.OneTimeWorkRequestBuilder<com.dollyplastic.invoiceapp.workers.InvoiceProcessingWorker>()
            .setInputData(
                androidx.work.workDataOf(
                    "invoiceId" to invoiceId,
                    "action" to "PROCESS_RESULT",
                    "resultFilePath" to file.absolutePath
                )
            )
            .build()

        androidx.work.WorkManager.getInstance(context).enqueue(workRequest)

        return@withContext WorkflowResult.ProcessingStarted
    }



}

sealed class WorkflowResult {
    object Completed : WorkflowResult()
    object ProcessingStarted : WorkflowResult()

    data class JsonReady(
        val file: File,
        val portalUrl: String
    ) : WorkflowResult()

    data class Error(
        val message: String
    ) : WorkflowResult()
}

