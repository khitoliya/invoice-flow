package com.dollyplastic.invoiceapp.domain.Parsing

import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails
import org.json.JSONObject
import java.io.File

import org.apache.poi.ss.usermodel.WorkbookFactory

import java.io.FileInputStream

object ResultParser {
    private const val TAG = "InvoiceWorkflow"

    fun parse(file: File): ParsingResult {
        android.util.Log.d(TAG, "[Parser] detecting file type for: ${file.name}")
        return when (detectFileType(file)) {
            FileType.JSON -> parseJson(file)
            FileType.EXCEL -> parseExcel(file)
            FileType.PDF -> parsePdf(file)
            FileType.TXT -> parseTxt(file)
            FileType.UNKNOWN ->
                ParsingResult.FileError("Unsupported file type: ${file.name}")
        }
    }

    // --------------------------------------------------
    // JSON PARSER (E-INVOICE / COMBINED RESPONSE)
    // --------------------------------------------------

    private fun parseJson(file: File): ParsingResult {
        val content = try {
            file.readText()
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[Parser] Failed to read JSON file", e)
            return ParsingResult.FileError("Unable to read JSON file")
        }

        val json = try {
            JSONObject(content)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[Parser] Invalid JSON format", e)
            return ParsingResult.FileError("Invalid JSON format")
        }

        // ---- NIC rejection block ----
        if (json.has("ErrorDetails") || json.has("error")) {
            val msg =
                json.optString("ErrorDetails")
                    .ifBlank { json.optString("error") }
                    .ifBlank { "Government portal rejected the data" }

            android.util.Log.w(TAG, "[Parser] JSON contains data error: $msg")
            return ParsingResult.DataError(msg)
        }

        // ---- Mandatory E-Invoice fields ----
        val irn = json.optString("Irn")
        val ackNo = json.optString("AckNo")
        val ackDate = json.optString("AckDt")

        if (irn.isBlank() || ackNo.isBlank()) {
            return ParsingResult.FileError("Missing IRN / AckNo in response file")
        }

        val eInvoice = EInvoiceDetails(
            irn = irn,
            ackNo = ackNo,
            ackDate = ackDate,
            signedQrCode = json.optString("SignedQRCode")
        )

        // ---- Optional E-Way Bill inside e-Invoice response ----
        val ewbNo = json.optString("EwbNo")
        val eWayBill =
            if (ewbNo.isNotBlank()) {
                EWayBillDetails(
                    ewayBillNo = ewbNo,
                    ewayBillDate = json.optString("EwbDt"),
                    validUpto = json.optString("EwbValidTill")
                )
            } else null
        
        android.util.Log.d(TAG, "[Parser] JSON parsed successfully. IRN: $irn")

        return ParsingResult.Success(
            eInvoiceDetails = eInvoice,
            eWayBillDetails = eWayBill
        )
    }

    // --------------------------------------------------
    // EXCEL PARSER (E-WAY BILL BULK RESULT)
    // --------------------------------------------------

    private fun parseExcel(file: File): ParsingResult {
        try {
            FileInputStream(file).use { fis ->
                val workbook = WorkbookFactory.create(fis)
                val sheet = workbook.getSheetAt(0)
                    ?: return ParsingResult.FileError("No sheet found in Excel file")

                val headerRow = sheet.getRow(0)
                    ?: return ParsingResult.FileError("Missing header row in Excel file")

                // Build header → column index map
                val headerIndex = mutableMapOf<String, Int>()
                headerRow.forEachIndexed { index, cell ->
                    headerIndex[cell.toString().trim()] = index
                }

                // First data row (NIC puts result in first row)
                val dataRow = sheet.getRow(1)
                    ?: return ParsingResult.FileError("No data row found in Excel file")

                // ---- Check for NIC error ----
                val errorMsg = readCell(dataRow, headerIndex, "Error Message")
                if (!errorMsg.isNullOrBlank()) {
                    return ParsingResult.DataError(errorMsg)
                }

                val ewbNo = readCell(dataRow, headerIndex, "EWB No")
                    ?: return ParsingResult.FileError("EWB No missing in result")

                val ewbDate = readCell(dataRow, headerIndex, "EWB Date")
                val validTill = readCell(dataRow, headerIndex, "Valid Till")

                val ewb = EWayBillDetails(
                    ewayBillNo = ewbNo,
                    ewayBillDate = ewbDate ?: "",
                    validUpto = validTill ?: ""
                )

                workbook.close()

                android.util.Log.d(TAG, "[Parser] Excel parsed successfully. EWB No: ${ewb.ewayBillNo}")

                return ParsingResult.Success(
                    eWayBillDetails = ewb
                )
            }
        } catch (e: Exception) {
            return ParsingResult.FileError("Failed to read Excel file: ${e.message}")
        }
    }

    // --------------------------------------------------
    // TXT PARSER (SCRAPED TEXT RESULT)
    // --------------------------------------------------

    private fun parseTxt(file: File): ParsingResult {
        return try {
            val content = file.readText()
            android.util.Log.d(TAG, "[Parser] Text File Content (First 500 chars): ${content.take(500)}")
            parseTextContent(content)
        } catch (e: Exception) {
            ParsingResult.FileError("Failed to read text file: ${e.message}")
        }
    }

    // --------------------------------------------------
    // PDF PARSER (PRINT -> SAVE AS PDF RESULT)
    // --------------------------------------------------
    
    private fun parsePdf(file: File): ParsingResult {
        android.util.Log.d(TAG, "[Parser] Starting PDF Parsing for: ${file.name}")
        try {
             com.itextpdf.kernel.pdf.PdfReader(file).use { reader ->
                com.itextpdf.kernel.pdf.PdfDocument(reader).use { pdfDoc ->
                    if (pdfDoc.numberOfPages == 0) return ParsingResult.FileError("Empty PDF file")
                    
                    val textStr = StringBuilder()
                    // Extract text from first page usually contains the header info
                    for (i in 1..java.lang.Math.min(pdfDoc.numberOfPages, 2)) {
                        textStr.append(com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor.getTextFromPage(pdfDoc.getPage(i)))
                        textStr.append("\n")
                    }
                    
                    val content = textStr.toString()
                    android.util.Log.d(TAG, "[Parser] PDF Content Extracted (First 500 chars): ${content.take(500)}")

                    return parseTextContent(content)
                }
             }
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[Parser] PDF Parsing Failed", e)
            return ParsingResult.FileError("Failed to parse PDF: ${e.message}")
        }
    }

    // --------------------------------------------------
    // SHARED TEXT CONTENT PARSER (FOR PDF/TXT)
    // --------------------------------------------------

    private fun parseTextContent(content: String): ParsingResult {
        // --- E-WAY BILL PARSING ---
        // Regex for E-Way Bill Number (12 digits, often labeled "E-Way Bill No")
        val ewbPattern = Regex("E-Way Bill No[\\s\\S]*?([0-9]{4}\\s*[0-9]{4}\\s*[0-9]{4})", RegexOption.IGNORE_CASE)
        // Update Date Regex to capture optional time (e.g. 11/01/2026 22:49:00)
        val datePattern = Regex("E-Way Bill Date[\\s\\S]*?(\\d{2}/\\d{2}/\\d{4}(?:\\s+\\d{2}:\\d{2}:\\d{2})?)", RegexOption.IGNORE_CASE)
        val validPattern = Regex("Valid Until[\\s\\S]*?(\\d{2}/\\d{2}/\\d{4})", RegexOption.IGNORE_CASE)
        
        val ewbMatch = ewbPattern.find(content)
        val dateMatch = datePattern.find(content)
        val validMatch = validPattern.find(content)
        
        var eWayBillDetails: EWayBillDetails? = null
        
        if (ewbMatch != null) {
            // Cleanup spaces from the captured group
            val ewbNo = ewbMatch.groupValues[1].replace("\\s".toRegex(), "")
            val ewbDate = dateMatch?.groupValues?.get(1)?.trim() ?: ""
            val validUpto = validMatch?.groupValues?.get(1) ?: ""

            // Extract 'Generated By' and clean to GSTIN only (First 15 chars)
            val generatedByPattern = Regex("Generated By\\s*[:\\-]?\\s*(.*)", RegexOption.IGNORE_CASE)
            val generatedByMatch = generatedByPattern.find(content)
            var generatedBy = generatedByMatch?.groupValues?.get(1)?.trim() ?: ""
            
            // Clean Generated By: Take the first word/token which should be the GSTIN (15 chars)
            if (generatedBy.length >= 15) {
                // Heuristic: If it contains " - ", take the part before it
                if (generatedBy.contains(" - ")) {
                    generatedBy = generatedBy.substringBefore(" - ").trim()
                } else {
                    // Fallback: take first 15 chars if they look like alphanumeric
                     val potentialGstin = generatedBy.take(15)
                     if (potentialGstin.matches(Regex("[A-Z0-9]{15}"))) {
                         generatedBy = potentialGstin
                     }
                }
            }

            // Extract 'Distance' from 'Valid From' line
            val distancePattern = Regex("\\[(\\d+)\\s*Kms\\]", RegexOption.IGNORE_CASE)
            val distanceMatch = distancePattern.find(content)
            val distance = distanceMatch?.groupValues?.get(1)?.toIntOrNull() ?: 0

            // Extract 'Valid From'
            var validFromPattern = Regex("Valid From\\s*[:\\-]?\\s*(\\d{2}/\\d{2}/\\d{4}\\s+\\d{1,2}:\\d{2}\\s+[AP]M)", RegexOption.IGNORE_CASE)
            var validFromMatch = validFromPattern.find(content)
            var validFrom = validFromMatch?.groupValues?.get(1)?.trim() ?: ""
            
            // Fallback: If Valid From is empty, use E-Way Bill Date (if it has time)
            if (validFrom.isBlank() && ewbDate.contains(":")) {
                validFrom = ewbDate
                android.util.Log.d(TAG, "[Parser] Valid From missing. Using EWB Date: $validFrom")
            }

            eWayBillDetails = EWayBillDetails(
                ewayBillNo = ewbNo,
                ewayBillDate = if (ewbDate.length > 10) ewbDate.take(10) else ewbDate,
                validUpto = validUpto,
                generatedBy = generatedBy,
                validFrom = validFrom, // This contains the timestamp
                distanceKm = distance
            )
             android.util.Log.d(TAG, "[Parser] Text Found EWB: ${eWayBillDetails.ewayBillNo}")
        } else {
             android.util.Log.d(TAG, "[Parser] Text did not contain E-Way Bill No.")
        }

        // --- E-INVOICE PARSING ---
        var eInvoiceDetails: EInvoiceDetails? = null
        
        val irnPattern = Regex("IRN\\s*[:\\-]?\\s*([a-zA-Z0-9]+)", RegexOption.IGNORE_CASE)
        val ackNoPattern = Regex("Ack No\\s*[:\\-]?\\s*([0-9]+)", RegexOption.IGNORE_CASE)
        val ackDatePattern = Regex("Ack Date\\s*[:\\-]?\\s*(.*)", RegexOption.IGNORE_CASE)
        // Status is often "ACT" or "Active"
        val statusPattern = Regex("Status\\s*[:\\-]?\\s*(.*)", RegexOption.IGNORE_CASE)
        
        val irnMatch = irnPattern.find(content)
        val ackNoMatch = ackNoPattern.find(content)
        
        if (irnMatch != null && ackNoMatch != null) {
            val irn = irnMatch.groupValues[1].trim()
            val ackNo = ackNoMatch.groupValues[1].trim()
            val ackDate = ackDatePattern.find(content)?.groupValues?.get(1)?.trim() ?: ""
            val status = statusPattern.find(content)?.groupValues?.get(1)?.trim() ?: "" // Not present in EInvoiceDetails? Check.
            
            // Check constructor: EInvoiceDetails(irn, ackNo, ackDate, signedQrCode)
            // We likely don't have Signed QR code from text scraping unless we scrape the long string
            eInvoiceDetails = EInvoiceDetails(
                irn = irn,
                ackNo = ackNo,
                ackDate = ackDate,
                signedQrCode = "" // Scraper doesn't extract QR yet.
            )
             android.util.Log.d(TAG, "[Parser] Text Found E-Invoice: IRN=$irn")
        }

        if (eWayBillDetails == null && eInvoiceDetails == null) {
            return ParsingResult.FileError("Could not find E-Way Bill OR E-Invoice details in content.")
        }
        
        return ParsingResult.Success(
            eWayBillDetails = eWayBillDetails,
            eInvoiceDetails = eInvoiceDetails
        )
    }

    private fun readCell(
        row: org.apache.poi.ss.usermodel.Row,
        headers: Map<String, Int>,
        columnName: String
    ): String? {
        val index = headers[columnName] ?: return null
        return row.getCell(index)?.toString()?.trim()
    }

    private fun detectFileType(file: File): FileType =
        when (file.extension.lowercase()) {
            "json" -> FileType.JSON
            "xls", "xlsx" -> FileType.EXCEL
            "pdf" -> FileType.PDF
            "txt" -> FileType.TXT // Support scraped text
            else -> FileType.UNKNOWN
        }

    private enum class FileType {
        JSON,
        EXCEL,
        PDF,
        TXT,
        UNKNOWN
    }
}

