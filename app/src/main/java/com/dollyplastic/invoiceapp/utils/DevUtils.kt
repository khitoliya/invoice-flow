package com.dollyplastic.invoiceapp.utils

import com.dollyplastic.invoiceapp.data.models.*
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

object DevUtils {

    suspend fun generateDummyInvoices(
        repository: InvoiceRepository,
        onProgress: suspend (Int, Int) -> Unit
    ) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        val todayStr = dateFormat.format(Date())

        // 1. Initialize Firm Details
        val dummyFirm = Firm(
            firmId = "9d6eb6ef-dc3e-4ca1-b5e6-9a50d7a71895",
            tradeName = "Dolly Plastic",
            nickName = "Dolly Plastic Rajasthan",
            gstin = "08ARDPK8616J1ZI",
            addressLine1 = "H-1,207 CHOPANKI INDUSTRIAL AREA",
            addressLine2 = "TEHSIL TIJARA",
            city = "Bhiwadi",
            pincode = "301019",
            stateCode = "08",
            state = "Rajasthan",
            bankName = "Kotak Mahindra Bank",
            accountNumber = "3812667485",
            ifscCode = "KKBK0000275",
            branchName = "Bhiwadi"
        )

        // 2. Initialize Dummy Item Details
        val dummyItem = Item(
            itemId = "7e312a62-b4bc-457a-8bee-e64f1228bee4",
            name = "Plastic Dana",
            normalizedName = "plasticdana",
            hsnCode = "39021000",
            unit = "KGS",
            gstRate = 18.00
        )

        // Create an InvoiceItem with standard quantity and rate (e.g., 100 qty * 100 rate = 10000 taxable value)
        val qty = 100.0
        val rate = 100.0
        val taxableValue = qty * rate
        val cgst = (taxableValue * 9) / 100
        val sgst = (taxableValue * 9) / 100
        val totalTax = cgst + sgst
        val totalValue = taxableValue + totalTax

        val dummyInvoiceItem = InvoiceItem(
            item = dummyItem,
            quantity = qty,
            rate = rate,
            taxableValue = taxableValue,
            gstAmount = cgst+sgst,
        )

        // 3. Loop to generate invoices starting from 19
        val start = 19
        val end = 199
        val total = end - start + 1
        var count = 0
        
        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            for (i in start..end) {
                val invoice = Invoice(
                invoiceId = UUID.randomUUID().toString(),
                invoiceSequence = i,
                invoiceNumber = i.toString(),
                invoiceDate = todayStr,
                financialYear = "2025-26",
                isCashSale = true,
                firm = dummyFirm,
                firmGstin = dummyFirm.gstin,
                items = listOf(dummyInvoiceItem),
                taxSummary = TaxSummary(
                    cgst = cgst,
                    sgst = sgst,
                    igst = 0.0,

                ),
                totalTaxableValue = taxableValue,
                totalTaxAmount = totalTax,
                totalInvoiceValue = totalValue,
                transportDetails = TransportDetails(
                    deliveryType = DeliveryType.BUYER_PICKUP
                ),
                status = InvoiceStatus.COMPLETED
                )

                // Save the invoice to repository
                repository.createInvoice(invoice)
                count++
                onProgress(count, total)
                kotlinx.coroutines.delay(20) // Give breathing room to DB and SyncManager
            }
        }
    }

    suspend fun fixDummyFinancialYear(
        repository: InvoiceRepository,
        onProgress: suspend (Int, Int) -> Unit
    ) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.NonCancellable) {
            val result = repository.getAllInvoices()
            if (result is com.dollyplastic.invoiceapp.data.utils.Result.Success) {
                // Find all incorrectly grouped invoices under "25-26"
                val toFix = result.data.filter { it.financialYear == "25-26" }
                val total = toFix.size
                var count = 0
                
                if (total == 0) {
                    onProgress(1, 1) // Signal completion if none
                    return@withContext
                }

                for (inv in toFix) {
                    // Update to correct financial year formatting matching the app
                    val patched = inv.copy(financialYear = "2025-26")
                    repository.createInvoice(patched)
                    count++
                    onProgress(count, total)
                    kotlinx.coroutines.delay(20)
                }
            }
        }
    }
}
