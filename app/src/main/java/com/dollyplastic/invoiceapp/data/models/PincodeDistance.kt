package com.dollyplastic.invoiceapp.data.models

import androidx.room.Entity

@Entity(tableName = "pincode_distances", primaryKeys = ["pincode1", "pincode2"])
data class PincodeDistance(
    val pincode1: String = "", // Always stored such that pincode1 < pincode2
    val pincode2: String = "",
    val distanceKm: Int = 0
)
