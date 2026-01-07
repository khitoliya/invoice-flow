package com.dollyplastic.invoiceapp.data.models

import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillDetails

enum class InvoiceStatus {
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
    PARSING_FAILED
}

data class Invoice(
    val invoiceId: String="",              // internal UUID
    val invoiceNumber: String="",
    val invoiceDate: String="",
    val financialYear: String = "", //derived from invoiceDate
    val invoiceSequence: Int = 0,

    val firm: Firm=Firm(),                      // SNAPSHOT (important)
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
    val processingError: String? = null
)
