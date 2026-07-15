package com.dollyplastic.invoiceapp.data.repository

import android.content.Context
import android.os.Environment
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Invoice
import java.io.File

object InvoiceStorage {

    private const val ROOT_DIR_NAME = "Invoice_App"
    private const val FINAL_PDF_DIR_NAME = "Invoice_Data"
    private const val MERGED_PDF_DIR_NAME = "Merged_Pdf"
    private const val TEMP_DIR_NAME = "Temporary_Files"

    // --- 1. Enumerated Types for Safety ---
    enum class TempFileType(val fileName: String) {
        PAYLOAD_EINVOICE("Payload_EInvoice.json"),
        PAYLOAD_EWAY("Payload_EWayBill.json"),
        PAYLOAD_COMBINED("Payload_Combined.json"),
        
        PORTAL_DOWNLOAD("Portal_Source_Document"), // extension dynamic
        MANUAL_UPLOAD("Manual_Source_Document"),   // extension dynamic
        PARSED_RESULT("Parsed_Result.json")
    }

    // --- 2. Directory Access (Downloads/Invoice_App) ---

    private fun getAppRoot(): File {
        val downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloads, ROOT_DIR_NAME).apply { mkdirs() }
    }

    // Returns: Downloads/Invoice_App/Invoice_Data/Dolly(07...)/FY 25-26/
    fun getFinalPdfDirectory(firm: Firm, financialYear: String): File {
        val firmId = getFirmIdentifier(firm)
        val fyDir = "FY $financialYear"
        
        // Structure: Invoice_Data / FirmId / FY
        val base = getInvoiceDataDirectory()
        val firmDir = File(base, firmId)
        return File(firmDir, fyDir).apply { mkdirs() }
    }
    
    fun getInvoiceDataDirectory(): File {
        return File(getAppRoot(), FINAL_PDF_DIR_NAME).apply { mkdirs() }
    }

    // Returns: Downloads/Invoice_App/Temporary_Files/Dolly(07...)_Invoice001/
    fun getTemporaryDirectory(firm: Firm, invoiceNumber: String): File {
        val firmId = getFirmIdentifier(firm)
        return getTemporaryDirectory(firmId, invoiceNumber)
    }

    // Overload for when only ID is available (e.g. from StorageRef)
    fun getTemporaryDirectory(firmIdentifier: String, invoiceNumber: String): File {
        val safeNum = sanitize(invoiceNumber)
        val uniqueName = "${sanitize(firmIdentifier)}_Invoice${safeNum}"
        
        val base = File(getAppRoot(), TEMP_DIR_NAME)
        return File(base, uniqueName).apply { mkdirs() }
    }


    // --- 3. File Access (Enforcing Naming) ---

    // STRICT FORMAT: INV_{Nick}_{FY}_{No}.pdf
    fun getFinalPdfFile(firm: Firm, invoice: Invoice): File {
        val dir = getFinalPdfDirectory(firm, invoice.financialYear)
        val safeNum = sanitize(invoice.invoiceNumber)
        
        // Validation: Nickname is MANDATORY now. Fallback to sanitary TradeName if missing (legacy safety)
        val alias = if (firm.nickName.isNotBlank()) firm.nickName else sanitize(firm.tradeName)
        
        val name = "INV_${alias}_${invoice.financialYear}_${safeNum}.pdf"
        return File(dir, name)
    }

    fun getTempFile(firm: Firm, invoiceNumber: String, type: TempFileType, extension: String? = null): File {
        val dir = getTemporaryDirectory(firm, invoiceNumber)
        val name = if (extension != null) "${type.fileName}.$extension" else type.fileName
        return File(dir, name)
    }

    // Returns: Downloads/Invoice_App/Merged_Pdf/Merged_{Timestamp}_{Count}_Invoices.pdf
    fun getMergedPdfFile(invoiceCount: Int): File {
        val base = File(getAppRoot(), MERGED_PDF_DIR_NAME).apply { mkdirs() }
        val timestamp = System.currentTimeMillis()
        val name = "Merged_${timestamp}_${invoiceCount}_Invoices.pdf"
        return File(base, name)
    }
    
    // --- 4. Cleanup ---
    
    fun deleteTempDirectory(firm: Firm, invoiceNumber: String): Boolean {
        val dir = getTemporaryDirectory(firm, invoiceNumber)
        return if (dir.exists()) {
            dir.deleteRecursively()
        } else {
            true
        }
    }


    // --- 5. Helpers ---

    fun getFirmIdentifier(firm: Firm): String {
        // "TradeName(GSTIN)" format as requested
        val safeTrade = sanitize(firm.tradeName)
        val safeGst = sanitize(firm.gstin)
        return "${safeTrade}($safeGst)"
    }

    private fun sanitize(value: String): String =
        value
            .trim()
            .replace("/", "-")
            .replace("\\", "-")
            .replace(Regex("[^a-zA-Z0-9_\\-\\(\\)]"), "_") // Allow (), -, _
            .replace(Regex("_+"), "_") // Collapse multiple underscores
}
