package com.dollyplastic.invoiceapp.data.models

data class Item(
    val itemId: String="",        // internal unique ID
    val name: String="",
    val hsnCode: String="",
    val unit: String="",
    val gstRate: Double=0.0,
    val normalizedName: String = ""
)
