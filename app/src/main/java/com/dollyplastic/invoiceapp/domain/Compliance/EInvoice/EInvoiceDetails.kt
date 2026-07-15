package com.dollyplastic.invoiceapp.domain.Compliance.EInvoice

data class EInvoiceDetails(
    val irn: String="",
    val ackNo: String="",
    val ackDate: String="",
    val signedQrCode: String="",
    val signedInvoiceData: String=""
)