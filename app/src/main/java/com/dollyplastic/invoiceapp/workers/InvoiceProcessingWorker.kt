package com.dollyplastic.invoiceapp.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.utils.Result as AppResult
import com.dollyplastic.invoiceapp.domain.Compliance.ComplianceResult
import com.dollyplastic.invoiceapp.domain.Compliance.ComplianceRunner
import com.dollyplastic.invoiceapp.domain.Workflow.InvoiceStatusUpdater
import com.dollyplastic.invoiceapp.pdf.InvoicePdfGenerator
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import androidx.work.ListenableWorker
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.domain.Parsing.ParsingResult
import com.dollyplastic.invoiceapp.domain.Parsing.ResultParser
import java.io.File

@HiltWorker
class InvoiceProcessingWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: InvoiceRepository,
    private val complianceRunner: ComplianceRunner,
    private val statusUpdater: InvoiceStatusUpdater
) : CoroutineWorker(appContext, workerParams) {

    private val TAG = "InvoiceWorker"

    override suspend fun doWork(): ListenableWorker.Result = withContext(Dispatchers.IO) {
        val invoiceId = inputData.getString("invoiceId") ?: return@withContext ListenableWorker.Result.failure()
        val action = inputData.getString("action") ?: "COMPLIANCE" // Default to original behavior
        val resultFilePath = inputData.getString("resultFilePath")

        Log.d(TAG, "[OPTIMIZED_FLOW] Starting Worker Action: $action for Invoice: $invoiceId")

        // 1. Load Invoice
        val invoiceResult = repository.getInvoice(invoiceId)
        val invoice = when(invoiceResult) {
            is AppResult.Success -> invoiceResult.data
            else -> {
                Log.e(TAG, "[OPTIMIZED_FLOW] Failed to load invoice $invoiceId")
                return@withContext ListenableWorker.Result.failure()
            }
        }

        return@withContext when(action) {
            "PROCESS_RESULT" -> {
                 if (resultFilePath == null) {
                     Log.e(TAG, "[OPTIMIZED_FLOW] Missing resultFilePath for PROCESS_RESULT")
                     return@withContext ListenableWorker.Result.failure()
                 }
                 processResult(invoice, File(resultFilePath))
            }
            else -> {
                // "COMPLIANCE"
                runComplianceCheck(invoice)
            }
        }
    }

    private suspend fun processResult(invoice: Invoice, file: File): ListenableWorker.Result {
         Log.d(TAG, "[OPTIMIZED_FLOW] Processing Result File: ${file.absolutePath}")
         
         // Transition to PROCESSING_RESULT (if not already)
         statusUpdater.transition(invoice.invoiceId, invoice.status, InvoiceStatus.PROCESSING_RESULT)

         try {
             // Parse
             val parsingResult = ResultParser.parse(file)
             
             when (parsingResult) {
                 is ParsingResult.Success -> {
                      Log.d(TAG, "[OPTIMIZED_FLOW] Parsing Success")
                      
                      parsingResult.eWayBillDetails?.let {
                          repository.attachEWayBill(invoice.invoiceId, it)
                      }
                      parsingResult.eInvoiceDetails?.let {
                          repository.attachEInvoice(invoice.invoiceId, it)
                      }
                      
                      // Generate Final PDF
                      InvoicePdfGenerator.generateForAndroid(appContext, repository.getInvoice(invoice.invoiceId).let { (it as AppResult.Success).data })
                      
                      // Mark Completed
                      statusUpdater.transition(invoice.invoiceId, InvoiceStatus.PROCESSING_RESULT, InvoiceStatus.COMPLETED)
                      
                      // Cleanup
                      try {
                          InvoiceStorage.deleteTempDirectory(invoice.firm, invoice.invoiceNumber)
                      } catch (e: Exception) {
                          Log.w(TAG, "[OPTIMIZED_FLOW] Cleanup failed", e)
                      }
                      
                      return ListenableWorker.Result.success()
                 }
                 is ParsingResult.DataError -> {
                      statusUpdater.transition(invoice.invoiceId, InvoiceStatus.PROCESSING_RESULT, InvoiceStatus.PARSING_FAILED, parsingResult.message)
                      return ListenableWorker.Result.failure()
                 }
                 is ParsingResult.FileError -> {
                      statusUpdater.transition(invoice.invoiceId, InvoiceStatus.PROCESSING_RESULT, InvoiceStatus.PARSING_FAILED, parsingResult.message)
                      return ListenableWorker.Result.failure()
                 }
             }
         } catch (e: Exception) {
             Log.e(TAG, "[OPTIMIZED_FLOW] Parsing Exception: ${e.message}", e)
             statusUpdater.transition(invoice.invoiceId, InvoiceStatus.PROCESSING_RESULT, InvoiceStatus.PARSING_FAILED, "Crash: ${e.message}")
             return ListenableWorker.Result.failure()
         }
    }

    private suspend fun runComplianceCheck(invoice: Invoice): ListenableWorker.Result {
        // 2. Simple Invoice Optimization
        if (!invoice.generateEInvoice && !invoice.generateEWayBill) {
            Log.d(TAG, "[OPTIMIZED_FLOW] Simple Invoice - Generating PDF")
            InvoicePdfGenerator.generateForAndroid(appContext, invoice)
            statusUpdater.transition(invoice.invoiceId, invoice.status, InvoiceStatus.COMPLETED)
            return ListenableWorker.Result.success()
        }

        // 3. Compliance Check
        try {
            Log.d(TAG, "[OPTIMIZED_FLOW] Running Compliance Checks")
            statusUpdater.transition(invoice.invoiceId, invoice.status, InvoiceStatus.GENERATING_JSON)
            
            val result = complianceRunner.run(appContext, invoice)
            when (result) {
                is ComplianceResult.ReadyForPortal -> {
                    Log.d(TAG, "[OPTIMIZED_FLOW] Compliance Success: JSON Ready")
                     statusUpdater.transition(invoice.invoiceId, InvoiceStatus.GENERATING_JSON, InvoiceStatus.WAITING_FOR_UPLOAD)
                     return ListenableWorker.Result.success()
                }
                is ComplianceResult.Error -> {
                    Log.e(TAG, "[OPTIMIZED_FLOW] Compliance Failed: ${result.message}")
                    statusUpdater.transition(invoice.invoiceId, InvoiceStatus.GENERATING_JSON, InvoiceStatus.JSON_GENERATION_FAILED, result.message)
                     return ListenableWorker.Result.failure()
                }
                is ComplianceResult.PdfOnly -> {
                     statusUpdater.transition(invoice.invoiceId, InvoiceStatus.GENERATING_JSON, InvoiceStatus.COMPLETED)
                     return ListenableWorker.Result.success()
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "[OPTIMIZED_FLOW] Unexpected Error", e)
            statusUpdater.transition(invoice.invoiceId, InvoiceStatus.GENERATING_JSON, InvoiceStatus.JSON_GENERATION_FAILED, e.message)
             return ListenableWorker.Result.failure()
        }
    }
}
