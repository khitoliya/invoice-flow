package com.dollyplastic.invoiceapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey
    val itemId: String="",        // internal unique ID
    val name: String="",
    val hsnCode: String="",
    val unit: String="",
    val gstRate: Double=0.0,
    val normalizedName: String = ""
)
