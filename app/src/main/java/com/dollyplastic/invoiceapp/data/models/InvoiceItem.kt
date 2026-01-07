package com.dollyplastic.invoiceapp.data.models

data class InvoiceItem(
    val item: Item=Item(),              // FULL SNAPSHOT of item master

    val quantity: Double=0.0,
    val rate: Double=0.0,

    val taxableValue: Double=0.0,
    val gstAmount: Double=0.0
)
