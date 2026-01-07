package com.dollyplastic.invoiceapp.data.models

data class EInvoiceDetails(
    val irn: String="",
    val ackNo: String="",
    val ackDate: String="",
    val signedQrCode: String="",
    val signedInvoiceData: String=""
)
