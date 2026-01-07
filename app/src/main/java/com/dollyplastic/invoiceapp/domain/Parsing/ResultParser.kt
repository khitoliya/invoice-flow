package com.dollyplastic.invoiceapp.domain.Parsing

import org.json.JSONObject
import java.io.File

data class PortalResult(
    val ackNo: String? = null,
    val ackDate: String? = null,
    val irn: String? = null,
    val signedQrCode: String? = null,
    val ewbNo: String? = null,
    val ewbDate: String? = null,
    val ewbValidTill: String? = null,
    val status: String? = null, // "Success" or "Error"
    val errorDetails: String? = null
)

object ResultParser {

    fun parse(file: File): PortalResult {
        // Read file content
        val content = file.readText()
        
        // Try parsing as JSON (Most common for API/Bulk tools)
        return try {
            val json = JSONObject(content)
            // Note: Structure varies by portal. This is a generic "best guess" based on standard NIC APIs
            // The user might need to adjust this based on the ACTUAL file format they download.
            
            // E-Invoice Response often has "AckNo", "Irn" at root or inside "Data"
            // E-Way Bill Response often has "EwbNo"
            
            PortalResult(
                ackNo = json.optString("AckNo").ifBlank { json.optString("ackNo") },
                ackDate = json.optString("AckDt").ifBlank { json.optString("ackDate") },
                irn = json.optString("Irn").ifBlank { json.optString("irn") },
                signedQrCode = json.optString("SignedQRCode").ifBlank { json.optString("signedQrCode") },
                ewbNo = json.optString("EwbNo").ifBlank { json.optString("ewayBillNo") },
                ewbDate = json.optString("EwbDt").ifBlank { json.optString("ewayBillDate") },
                ewbValidTill = json.optString("EwbValidTill").ifBlank { json.optString("validUpto") },
                status = if (json.optString("Status", "Success").equals("Success", true)) "Success" else "Error",
                errorDetails = json.optString("ErrorDetails")
            )
        } catch (e: Exception) {
            // If JSON fails, maybe it's a simple CSV or text? 
            // For now, return Error
            PortalResult(
                status = "Error",
                errorDetails = "Failed to parse JSON: ${e.message}"
            )
        }
    }
}
