package com.dollyplastic.invoiceapp.domain.Workflow

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.EInvoice.EInvoiceJsonSerializer
import com.dollyplastic.invoiceapp.domain.EInvoice.EInvoiceMapper
import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillDetails
import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillJsonSerializer
import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillMapper
import com.dollyplastic.invoiceapp.domain.Parsing.ResultParser
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoiceWorkflowManager @Inject constructor(
    private val repository: InvoiceRepository,
    private val eWayBillMapper: EWayBillMapper,
    private val eWayBillSerializer: EWayBillJsonSerializer,
    private val eInvoiceMapper: EInvoiceMapper,
    private val eInvoiceSerializer: EInvoiceJsonSerializer
) {

    // Step 1: Start Processing - Generate JSON
    suspend fun startProcessing(invoice: Invoice, outputDir: File): WorkflowResult {
        return try {
            repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.GENERATING_JSON)
            
            var jsonContent = ""
            var fileName = ""
            var targetPortalUrl = ""

            // Determine Workflow
            if (invoice.generateEInvoice) {
                // E-Invoice (covers both or just E-Invoice)
                // Note: If both are valid, the E-Invoice JSON includes EWB details
                val distance = invoice.transportDetails.distance
                val draft = eInvoiceMapper.mapInvoiceToEInvoiceDraft(invoice, distance)
                jsonContent = eInvoiceSerializer.serialize(draft)
                fileName = "Einvoice_${invoice.invoiceNumber}.json"
                targetPortalUrl = "https://einvoice1.gst.gov.in/" 
                
            } else if (invoice.generateEWayBill) {
                // Only E-Way Bill
                val draft = eWayBillMapper.mapToEWayBillDraft(invoice)
                val json = eWayBillSerializer.serialize(draft)
                jsonContent = json.toString()
                fileName = "EWayBill_${invoice.invoiceNumber}.json"
                targetPortalUrl = "https://ewaybillgst.gov.in/"
            } else {
                // Formatting/Logic error if we got here without compliance flags
                return WorkflowResult.Error("No compliance flags set")
            }

            // Write File
            val file = File(outputDir, fileName)
            file.writeText(jsonContent)

            // Update Status
            repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.WAITING_FOR_UPLOAD)
            
            WorkflowResult.JsonGenerated(file, targetPortalUrl)
            
        } catch (e: Exception) {
            val errorMsg = "JSON Generation Failed: ${e.message}"
            repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.JSON_GENERATION_FAILED, errorMsg)
            WorkflowResult.Error(errorMsg)
        }
    }

    // Step 2: Handle Result Parser
    suspend fun onResultFileDownloaded(invoice: Invoice, file: File): WorkflowResult {
        return try {
            repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.PROCESSING_RESULT)
            
            val result = ResultParser.parse(file)
            
            if (result.status == "Success") {
                // Update Invoice with Parsed Details
                
                if (result.ewbNo != null) {
                   val ewbDetails = EWayBillDetails(
                       ewayBillNo = result.ewbNo,
                       ewayBillDate = result.ewbDate ?: "",
                       validUpto = result.ewbValidTill ?: ""
                   )
                   repository.attachEWayBill(invoice.invoiceId, ewbDetails)
                }

                if (result.irn != null) {
                    val eInvDetails = EInvoiceDetails(
                        irn = result.irn,
                        ackNo = result.ackNo ?: "",
                        ackDate = result.ackDate ?: "",
                        signedQrCode = result.signedQrCode ?: ""
                    )
                    repository.attachEInvoice(invoice.invoiceId, eInvDetails)
                }

                repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.COMPLETED)
                WorkflowResult.Success
            } else {
                val errorMsg = "Portal Error: ${result.errorDetails ?: "Unknown"}"
                 repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.PARSING_FAILED, errorMsg)
                WorkflowResult.Error(errorMsg)
            }
            
        } catch (e: Exception) {
             val errorMsg = "Parsing Failed: ${e.message}"
             repository.updateInvoiceStatus(invoice.invoiceId, InvoiceStatus.PARSING_FAILED, errorMsg)
             WorkflowResult.Error(errorMsg)
        }
    }
}

sealed class WorkflowResult {
    data class JsonGenerated(val file: File, val portalUrl: String) : WorkflowResult()
    object Success : WorkflowResult()
    data class Error(val message: String) : WorkflowResult()
}
