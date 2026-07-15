package com.dollyplastic.invoiceapp.domain.Compliance


import android.content.Context
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceJsonSerializer
import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceMapper
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillJsonSerializer

import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillMapper
import com.dollyplastic.invoiceapp.domain.Validation.EInvoiceValidator
import com.dollyplastic.invoiceapp.domain.Validation.EWayBillValidator

import com.dollyplastic.invoiceapp.domain.Validation.InvoiceValidator
import com.dollyplastic.invoiceapp.domain.Validation.ValidationLevel
import java.io.File


import com.dollyplastic.invoiceapp.domain.Validation.ValidationResult
import javax.inject.Inject

class ComplianceRunner @Inject constructor(
    private val repository: InvoiceRepository,
    private val eWayBillMapper: EWayBillMapper,
    private val eWayBillSerializer: EWayBillJsonSerializer,
    private val eInvoiceMapper: EInvoiceMapper,
    private val eInvoiceSerializer: EInvoiceJsonSerializer
) {
    private val TAG = "InvoiceWorkflow"

    fun run(
        context: Context,
        invoice: Invoice
    ): ComplianceResult {

        /* ---------------- DETERMINE VALIDATION LEVEL ---------------- */


        val level =
            when {
                invoice.generateEInvoice -> ValidationLevel.E_INVOICE
                invoice.generateEWayBill -> ValidationLevel.E_WAY
                else -> ValidationLevel.BASE
            }

        /* ---------------- INVOICE VALIDATION ---------------- */
        
        android.util.Log.d(TAG, "[Compliance] Starting validation for Invoice: ${invoice.invoiceId}, Level: $level")

        // Simulated error for testing



        val invoiceValidation = InvoiceValidator.validate(invoice, level)
        if (invoiceValidation is ValidationResult.Invalid) {
            android.util.Log.e(TAG, "[Compliance] Invoice Validation Failed: ${invoiceValidation.errors.size} errors")
            return ComplianceResult.Error(
                invoiceValidation.errors.joinToString("\n") {
                    "${it.section}: ${it.message}"
                }
            )
        }

        /* ---------------- OUTPUT DIR ---------------- */


        /* ---------------- ROUTING ---------------- */


        return when {



            !invoice.generateEInvoice && !invoice.generateEWayBill -> {
                ComplianceResult.PdfOnly
            }

            invoice.generateEInvoice -> {
                handleEInvoice(context, invoice)
            }

            invoice.generateEWayBill -> {
                handleEWayBill(context, invoice)
            }

            else -> ComplianceResult.Error("Unsupported compliance configuration")
        }
    }

    /* ================== PRIVATE ================== */

    private fun handleEWayBill(
        context: Context,
        invoice: Invoice
    ): ComplianceResult {

        val draft = eWayBillMapper.mapInvoiceToEWayBillDraft(invoice)

        val validation = EWayBillValidator.validate(draft)
        if (validation is ValidationResult.Invalid) {
            return ComplianceResult.Error(
                validation.errors.joinToString("\n") {
                    "${it.section}: ${it.message}"
                }
            )
        }

        val json = eWayBillSerializer.serialize(draft)

        val file = InvoiceStorage.getTempFile(
            firm = invoice.firm,
            invoiceNumber = invoice.invoiceNumber,
            type = InvoiceStorage.TempFileType.PAYLOAD_EWAY
        )
        // Ensure dir exists
        file.parentFile?.mkdirs()
        
        file.writeText(json.toString(2))
        android.util.Log.d(TAG, "[Compliance] Generated E-Way Bill JSON at path: ${file.absolutePath}")
        android.util.Log.d(TAG, "[Compliance] Financial Year: ${invoice.financialYear}")

        // Trigger MediaScanner to ensure file picker sees the file immediately (especially if folder reused)
        android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { path, uri ->
            android.util.Log.d(TAG, "[Compliance] MediaScanner scanned: $path -> $uri")
        }

        return ComplianceResult.ReadyForPortal(
            portalUrl = "https://ewaybillgst.gov.in/Login.aspx",
            jsonFile = file
        )
    }

    private fun handleEInvoice(
        context: Context,
        invoice: Invoice
    ): ComplianceResult {

        val draft = eInvoiceMapper.mapInvoiceToEInvoiceDraft(invoice)

        val validation = EInvoiceValidator.validate(draft)
        if (validation is ValidationResult.Invalid) {
            return ComplianceResult.Error(
                validation.errors.joinToString("\n") {
                    "${it.section}: ${it.message}"
                }
            )
        }

        val json = eInvoiceSerializer.serialize(draft)

        val file = InvoiceStorage.getTempFile(
            firm = invoice.firm,
            invoiceNumber = invoice.invoiceNumber,
            type = InvoiceStorage.TempFileType.PAYLOAD_EINVOICE
        )
        // Ensure dir exists
        file.parentFile?.mkdirs()

        file.writeText(json.toString(2))
        android.util.Log.d(TAG, "[Compliance] Generated E-Invoice JSON at path: ${file.absolutePath}")
        android.util.Log.d(TAG, "[Compliance] Financial Year: ${invoice.financialYear}")

        // Trigger MediaScanner to ensure file picker sees the file immediately (especially if folder reused)
        android.media.MediaScannerConnection.scanFile(context, arrayOf(file.absolutePath), null) { path, uri ->
            android.util.Log.d(TAG, "[Compliance] MediaScanner scanned: $path -> $uri")
        }

        return ComplianceResult.ReadyForPortal(
            portalUrl = "https://einvoice1.gst.gov.in/",
            jsonFile = file
        )
    }
}

