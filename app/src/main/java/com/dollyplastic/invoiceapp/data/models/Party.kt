package com.dollyplastic.invoiceapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parties")
data class Party(
    @PrimaryKey
    val partyId: String="",           // internal unique ID
    val tradeName: String="",
    val nickName: String="",          // handles same buyer, diff address

    val gstin: String="",             // mandatory

    val addressLine1: String="",
    val addressLine2: String?=null,
    val city: String="",
    val state: String="",
    val stateCode: String="",
    val pincode: String=""
)
