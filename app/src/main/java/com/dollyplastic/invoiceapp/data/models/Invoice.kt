package com.dollyplastic.invoiceapp.data.models

import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails
import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InvoiceStatus {
    PREVIEW_CONFIRMED,
    DRAFT,
    PENDING_VALIDATION,

    // Processing Stages
    GENERATING_JSON,
    WAITING_FOR_UPLOAD, // showing WebView
    PROCESSING_RESULT,  // Parsing downloaded file

    // Success
    COMPLETED,

    // Failure States (Granular)
    VALIDATION_FAILED,
    JSON_GENERATION_FAILED,
    UPLOAD_FAILED,      // or Cancelled by user
    PARSING_FAILED,
    CANCELLED
}
enum class ParsingFailureType {
    FILE_ERROR,     // corrupt / missing / wrong file
    DATA_ERROR      // NIC rejected / validation error
}



@Entity(
    tableName = "invoices",
    indices = [
        androidx.room.Index(value = ["status"]),
        androidx.room.Index(value = ["invoiceDate"]),
        androidx.room.Index(value = ["firmGstin"]),
        androidx.room.Index(value = ["invoiceNumber"]),
        androidx.room.Index(value = ["invoiceDateEpoch"])
    ]
)
data class Invoice(
    @PrimaryKey
    val invoiceId: String="",              // internal UUID
    val invoiceNumber: String="",
    val invoiceDate: String="",
    val invoiceDateEpoch: Long = 0,        // Optimization for sorting
    val financialYear: String = "", //derived from invoiceDate
    val invoiceSequence: Int = 0,
    
    val sourcePdfPath: String? = null, // Path to imported PDF

    val firm: Firm=Firm(),                      // SNAPSHOT (important)
    val firmGstin: String = firm.gstin, // Extracted for querying
    @field:JvmField
    val isCashSale: Boolean=false,
    val billToParty: Party? = null,   // Buyer (Bill To)

    // Shipping
    val shipToParty: Party? = null,   // Consignee (Ship To)


    val items: List<InvoiceItem> =emptyList<InvoiceItem>(),

    val taxSummary: TaxSummary= TaxSummary(),
    val totalTaxableValue: Double=0.0,
    val totalTaxAmount: Double=0.0,
    val totalInvoiceValue: Double=0.0,

    val transportDetails: TransportDetails= TransportDetails(),
    val additionalDetails: AdditionalDetails?=null,

    val generateEInvoice: Boolean = false,
    val generateEWayBill: Boolean = false,

    val eInvoiceDetails: EInvoiceDetails?=null,   // nullable, updatable later
    val eWayBillDetails: EWayBillDetails?=null,    // nullable, updatable later

    val status: InvoiceStatus = InvoiceStatus.DRAFT,
    val processingError: String? = null,
    val parsingFailureType: ParsingFailureType? = null,
    
    val updatedAt: Long = System.currentTimeMillis()

)
