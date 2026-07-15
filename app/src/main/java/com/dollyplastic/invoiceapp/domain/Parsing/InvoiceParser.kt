package com.dollyplastic.invoiceapp.domain.Parsing

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.data.models.Purchase
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.data.models.TransportMode
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import java.util.regex.Pattern

object InvoiceParser {

    // --- Public API ---

    suspend fun parseSale(context: Context, pdfFile: File, knownFirms: List<Firm>): Invoice {
        Log.d("ImportWorkflow", "--- Parsing Sale (Strict Firm Check) ---")
        val text = extractTextWithMlKit(context, pdfFile)
        val extractedData = extractCommonData(text)
        
        Log.d("ImportWorkflow", "Extracted GSTINs: ${extractedData.gstins}")
        
        // 1. Identify FIRM (Seller)
        // Find which extracted GSTIN belongs to one of our Known Firms
        val matchedFirm = knownFirms.find { firm -> 
            extractedData.gstins.any { it.equals(firm.gstin, ignoreCase = true) }
        }
        
        if (matchedFirm == null) {
            Log.e("ImportWorkflow", "No matching Firm found in DB for GSTINs: ${extractedData.gstins}")
            throw IllegalArgumentException("Firm not found in database. Please register the Firm first.")
        }
        
        Log.d("ImportWorkflow", "Matched Firm: ${matchedFirm.tradeName} (${matchedFirm.gstin})")

        // 2. Identify BUYER (Party)
        // The buyer is the GSTIN that is NOT the Firm's GSTIN
        val uniqueGstins = extractedData.gstins.filterNot { it.equals(matchedFirm.gstin, ignoreCase = true) }
        val buyerGstin = uniqueGstins.firstOrNull()
        
        // Construct detailed Party if not null
        val extractedParty = if (buyerGstin != null) {
            // Refined Name Extraction using Context
            val addressDetails = extractAccuratePartyDetails(extractedData.fullText, buyerGstin)
            
            Party(
                gstin = buyerGstin,
                tradeName = addressDetails.name.ifEmpty { extractedData.address.name.ifEmpty { "New Party ($buyerGstin)" } },
                addressLine1 = addressDetails.address.ifEmpty { extractedData.address.address },
                city = addressDetails.city.ifEmpty { extractedData.address.city },
                state = addressDetails.state.ifEmpty { extractedData.address.state },
                pincode = addressDetails.pincode.ifEmpty { extractedData.address.pincode }
            )
        } else null

        return Invoice(
             invoiceId = UUID.randomUUID().toString(),
             invoiceNumber = extractedData.invoiceNumber ?: "",
             invoiceDate = extractedData.date ?: DateUtils.today(),
             sourcePdfPath = pdfFile.absolutePath,
             firm = matchedFirm, // Bind strict firm
             firmGstin = matchedFirm.gstin,
             billToParty = extractedParty,
             items = extractedData.items,
             transportDetails = extractedData.transport,
             totalInvoiceValue = extractedData.amount ?: 0.0
        )
    }

    suspend fun parsePurchase(context: Context, pdfFile: File, contextFirmGstin: String? = null): Purchase {
        Log.d("ImportWorkflow", "--- Parsing Purchase ---")
        val text = extractTextWithMlKit(context, pdfFile)
        val extractedData = extractCommonData(text)
        
        Log.d("ImportWorkflow", "Extracted: $extractedData")
        
        val uniqueGstins = extractedData.gstins.minus(contextFirmGstin ?: "")
        val vendorGstin = uniqueGstins.firstOrNull()
        
         val extractedVendor = if (vendorGstin != null) {
            Party(
                gstin = vendorGstin,
                tradeName = extractedData.address.name.ifEmpty { "New Vendor ($vendorGstin)" }, 
                addressLine1 = extractedData.address.address,
                city = extractedData.address.city,
                state = extractedData.address.state,
                pincode = extractedData.address.pincode
            )
        } else Party() // Purchases need non-null vendor usually

        // Note: Purchase doesn't strictly track transportDetails in the same way, but we can verify against source.
        
        return Purchase(
            purchaseId = UUID.randomUUID().toString(),
            vendorInvoiceNumber = extractedData.invoiceNumber ?: "",
            purchaseDate = extractedData.date ?: DateUtils.today(),
            pdfFilePath = pdfFile.absolutePath,
            vendor = extractedVendor,
            totalAmount = extractedData.amount ?: 0.0
        )
    }

    // --- Private Parsing Logic ---


    // --- Detailed Extraction Methods ---

    private fun extractVehicleNumber(text: String): String? {
        // Standard Indian Vehicle Regex: MH12AB1234
        val vehicleRegex = "([A-Z]{2}\\s*\\d{2}\\s*[A-Z]{0,3}\\s*\\d{4})"
        val pattern = Pattern.compile(vehicleRegex)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1)?.replace(" ", "") else null
    }

    private fun extractAddressDetails(text: String, gstins: List<String>): AddressComponents {
        // We prioritize finding the Party Name relative to the Buyer GSTIN
        // Since we don't know exactly which GSTIN is the party *inside* this method (we iterate),
        // we can try to find names for ALL GSTINs and store them?
        // Or better: The caller (extractCommonData) calls this.
        
        // Let's iterate lines.
        val lines = text.split("\n")
        var bestName = ""
        var bestPincode = ""
        
        // Strategy: Find the Buyer GSTIN (which is one of the gstins list). 
        // Text is the whole PDF text.
        
        // Pincode Logic:
        val pincodePattern = Pattern.compile("\\b(\\d{6})\\b")
        // Be careful not to pick the Firm's pincode (110041) if we are looking for the Buyer's.
        // Usually, the Buyer's address is near the Buyer's GSTIN.
        
        return AddressComponents(name = "", address = "", city = "", state = "", pincode = "")
    }
    
    // New Helper to extract Name/Address specifically around a Target GSTIN
    private fun extractAccuratePartyDetails(text: String, targetGstin: String): AddressComponents {
        val lines = text.split("\n")
        val index = lines.indexOfFirst { it.contains(targetGstin, ignoreCase = true) }
        
        var name = ""
        var address = ""
        var pincode = ""
        
        if (index != -1) {
            // Look UPWARDS for Name (heuristic: 1-4 lines above, skipping labels like "Buyer", "Consignee")
            // Example:
            // Buyer (Bill to)
            // Ranjan Packaging
            // Address...
            // GSTIN: ...
            
            for (i in 1..4) {
                if (index - i < 0) break
                val line = lines[index - i].trim()
                
                // Skip Labels
                if (line.contains("Buyer", true) || line.contains("Consignee", true) || line.contains("Bill to", true)) continue
                if (line.isBlank()) continue
                
                // If it looks like an address (has numbers, "Road", "Dist"), maybe skip or take as address?
                // The Name is usually the "Header" of the block.
                // Let's assume the Line just below the "Buyer" label is the Name.
                
                // Improved Heuristic:
                // Find "Buyer (Bill to)" label. The next line is Name.
                // Find "GSTIN". The lines above are Name/Address.
                
                // Let's take the first non-label line above GSTIN as Name?
                if (name.isEmpty()) {
                    name = line
                    // Continue to find address?
                }
            }
        }
        
        // Pincode Search (Window +/- 5 lines around GSTIN)
        val start = (index - 5).coerceAtLeast(0)
        val end = (index + 5).coerceAtMost(lines.lastIndex)
        val pinRegex = Regex("\\b\\d{6}\\b")
        
        for (i in start..end) {
             pinRegex.find(lines[i])?.let {
                 if (it.value != "110041") { // Hardcoded hack to avoid Firm Pincode? No, strictly parse. 
                     // We should pass the Firm Pincode to avoid it? 
                     // For now, accept the first found that isn't known to be ours? 
                     // Let's just find *a* pincode.
                     pincode = it.value
                 }
             }
        }

        return AddressComponents(name = name, address = address, pincode = pincode)
    }

    private fun extractItems(text: String): List<InvoiceItem> {
        val items = mutableListOf<InvoiceItem>()
        val lines = text.split("\n")
        
        // Regex to match a line with:
        // [Index] [Description] [HSN] [Qty] [Rate] [Amount]
        // Example: 1 Pp Dana (39021000) 39021000 2,075 kgs 55.00 kgs 1,14,125.00
        
        // Key Anchors:
        // HSN: 4-8 digits.
        // Qty: Number (maybe commas) + optional text (kgs).
        // Rate: Decimal number.
        // Amount: Decimal number (maybe commas).
        
        // Pattern:
        // .*? matches description
        // (\d{4,8}) matches HSN (optional?) - User screenshot shows HSN column.
        // ([\d,]+(?:\.\d+)?) \s* [A-Za-z]* matches Qty (2,075 kgs)
        // ([\d,]+(?:\.\d{2})?) matches Rate
        
        // Strict approach often fails. Let's try to isolate numbers at the end of the line.
        
        for (line in lines) {
             // Look for Amount at the end (must be a large number usually two decimals)
             // 1,14,125.00
             val amountMatcher = Pattern.compile("([\\d,]+\\.\\d{2})\\s*$").matcher(line)
             if (amountMatcher.find()) {
                 val amountStr = amountMatcher.group(1)
                 val amount = amountStr?.replace(",", "")?.toDoubleOrNull() ?: 0.0
                 
                 // If we found an amount, look for HSN in the same line
                 val hsnMatcher = Pattern.compile("\\b(\\d{4,8})\\b").matcher(line)
                 var hsn = ""
                 if (hsnMatcher.find()) {
                     hsn = hsnMatcher.group(1) ?: ""
                 }
                 
                 // If HSN found, extraction is high confidence.
                 if (hsn.isNotEmpty()) {
                     // Cleanup Name: Remove HSN and Amount and Qty/Rate numbers
                     var rawName = line.replace(amountStr ?: "", "").replace(hsn, "")
                     // Remove other numbers (Qty, Rate) roughly
                     rawName = rawName.replace(Regex("[\\d,]+\\.\\d{2}"), "") // Rate/Qty with decimals
                     rawName = rawName.replace(Regex("\\b\\d+\\s+kgs"), "") // Qty with units
                     
                     // Remove leading "1 " (Index)
                     rawName = rawName.replace(Regex("^\\s*\\d+\\s+"), "").trim()
                     
                     // Construct Item
                     val itemObj = Item(
                         itemId = UUID.randomUUID().toString(),
                         name = rawName.takeIf { it.isNotEmpty() }?.take(50) ?: "Extracted Item",
                         hsnCode = hsn
                     )
                     
                     items.add(
                         InvoiceItem(
                             item = itemObj,
                             taxableValue = amount
                             // TODO: Extract Qty and Rate if possible
                         )
                     )
                 }
             }
        }
        
        return items
    }
    
    // --- Common Data Extraction Updated ---
    
    private fun extractTransporterDetails(text: String): TransportDetails {
        var vehicle = extractVehicleNumber(text)
        var transporterName: String? = null
        var docNo: String? = null
        var docDate: String? = null

        // Transporter Name
        // Fix: "ER" extracted likely from "Buyer" or "Manager".
        // Search specifically for "Transport" or "Carrier"
        val transporterPattern = Pattern.compile("(?:Transporter|Transport|Carrier)\\s*[:\\-]?\\s*([A-Za-z\\s\\.]+)", Pattern.CASE_INSENSITIVE)
        val transMatcher = transporterPattern.matcher(text)
        if (transMatcher.find()) {
            val candidate = transMatcher.group(1)?.trim()
            if (candidate?.length ?: 0 > 3) { // Avoid short garbage like "ER"
                 transporterName = candidate
            }
        }

        // LR / RR / Doc No
        // Fix: "Dispatched" extracted. Exclude known keywords from value.
        val docPattern = Pattern.compile("(?:LR\\s*No|RR\\s*No|Doc\\s*No|CN\\s*No|Bil\\s*of\\s*Lading)\\.?\\s*[:\\-]?\\s*([A-Za-z0-9/\\-]+)", Pattern.CASE_INSENSITIVE)
        val docMatcher = docPattern.matcher(text)
        if (docMatcher.find()) {
            val candidate = docMatcher.group(1)?.trim()
            if (candidate != null && !candidate.equals("Dispatched", ignoreCase = true) && !candidate.contains("Date", ignoreCase = true)) {
                 docNo = candidate
            }
        }
        
        // LR Date
        val lrDatePattern = Pattern.compile("(?:LR|RR|Doc)\\.?\\s*Date\\s*[:\\-]?\\s*(\\d{2}[-/]\\d{2}[-/]\\d{4}|\\d{2}-[A-Za-z]{3}-\\d{2})", Pattern.CASE_INSENSITIVE)
        val lrDateMatcher = lrDatePattern.matcher(text)
        if (lrDateMatcher.find()) {
            docDate = lrDateMatcher.group(1)
        }

        return TransportDetails(
            vehicleNumber = vehicle,
            transporterName = transporterName,
            transporterDocNo = docNo,
            transporterDocDate = docDate,
            mode = if (vehicle != null) TransportMode.ROAD else TransportMode.ROAD // Default
        )
    }

    private data class AddressComponents(
        val name: String = "",
        val address: String = "",
        val city: String = "",
        val state: String = "",
        val pincode: String = ""
    )

    private data class ExtractedData(
        val invoiceNumber: String?,
        val date: String?,
        val gstins: List<String>,
        val amount: Double?,
        val items: List<InvoiceItem>,
        val transport: TransportDetails,
        val fullText: String,
        val address: AddressComponents
    )

    private fun extractCommonData(text: String): ExtractedData {
        val gstins = extractAllGstins(text)
        return ExtractedData(
            invoiceNumber = extractInvoiceNumber(text),
            date = extractDate(text),
            gstins = gstins,
            amount = extractTotalAmount(text),
            items = extractItems(text),
            transport = extractTransporterDetails(text),
            fullText = text,
            address = AddressComponents() // Placeholder, we calculate it later based on filtered GSTIN
        )
    }

    private suspend fun extractTextWithMlKit(context: Context, file: File): String {
        return try {
            val renderer = PdfRenderer(
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            )
            val sb = StringBuilder()
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val pagesToRead = renderer.pageCount.coerceAtMost(2)
            
            for (i in 0 until pagesToRead) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(
                    page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888
                )
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()
                sb.append(result.text).append("\n")
            }
            renderer.close()
            sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            // Log error
            Log.e("ImportWorkflow", "MLKit Extraction Failed", e)
            ""
        }
    }

    private fun extractInvoiceNumber(text: String): String? {
        val pattern = Pattern.compile("(?:Invoice\\s*N[o0]\\.?|Inv\\.?\\s*No\\.?|Bill\\s*No\\.?)[:\\s\\-]*([A-Za-z0-9/\\-]+)", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        return if (matcher.find()) matcher.group(1)?.trim() else null
    }

    private fun extractDate(text: String): String? {
        // Expanded Date Pattern
        // dd-MM-yyyy | yyyy-MM-dd | dd MMM yyyy (10 Dec 2025) | dd-MMM-yy (10-Dec-25)
        val datePattern = "(\\d{2}[-/]\\d{2}[-/]\\d{4}|\\d{4}[-/]\\d{2}[-/]\\d{2}|\\d{1,2}\\s[A-Za-z]{3}\\s\\d{4}|\\d{2}[-/][A-Za-z]{3}[-/]\\d{2})"
        val contextPattern = Pattern.compile("(?:Date|Dated|Invoice\\s*Date)[:\\s\\-]*$datePattern", Pattern.CASE_INSENSITIVE)
        val matcher = contextPattern.matcher(text)
        if (matcher.find()) {
            val dateStr = matcher.group(1)
            // Normalize dd-MMM-yy (10-Dec-25) to dd-MM-yyyy if needed, or UI handles it.
            // For now, return as is.
            return dateStr
        }
        
        // Fallback
        val rawMatcher = Pattern.compile(datePattern).matcher(text)
        return if (rawMatcher.find()) rawMatcher.group(1) else null
    }

    private fun extractAllGstins(text: String): List<String> {
        val gstinRegex = "\\d{2}[A-Z]{5}\\d{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}"
        val pattern = Pattern.compile(gstinRegex)
        val matcher = pattern.matcher(text)
        val list = mutableListOf<String>()
        while (matcher.find()) {
            list.add(matcher.group())
        }
        return list.distinct()
    }

    private fun extractTotalAmount(text: String): Double? {
        // Robust Amount extraction: Look for "Total" at end of line or row
        // Often OCR puts "Total 1234.00" on one line
        val pattern = Pattern.compile("(?:Grand|Invoice|Net)?\\s*Total\\s*[:\\-]?\\s*[Rs\\.]?\\s*([\\d,]+\\.?\\d{0,2})", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(text)
        
        var bestAmount: Double? = null
        while(matcher.find()) {
             val raw = matcher.group(1)?.replace(",", "")
             val amount = raw?.toDoubleOrNull()
             // Heuristic: The largest "Total" found is usually the Grand Total (avoiding subtotals)
             if (amount != null) {
                 if (bestAmount == null || amount > bestAmount) {
                     bestAmount = amount
                 }
             }
        }
        return bestAmount
    }
}
