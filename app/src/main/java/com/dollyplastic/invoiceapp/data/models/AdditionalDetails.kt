package com.dollyplastic.invoiceapp.data.models

data class AdditionalDetails(
    val paymentMode: PaymentMode? = null,     // CHEQUE / RTGS / etc
    val deliveryNoteNo: String? = null,
    val deliveryNoteDate: String? = null,
    val buyerOrderNo: String? = null,
    val referenceNo: String? = null,
    val referenceDate: String? = null,
    val otherReferences: String? = null,
    val termsOfDelivery: String? = null
)
enum class PaymentMode{
    CHEQUE,
    RTGS,
    NEFT,
    UPI,
    CASH,
    OTHER
}
