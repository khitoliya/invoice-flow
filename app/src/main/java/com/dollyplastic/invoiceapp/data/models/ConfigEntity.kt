package com.dollyplastic.invoiceapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "configurations")
data class ConfigEntity(
    @PrimaryKey
    val key: String,
    val valueJson: String // Stores JSON representation of the config (List<Double>, List<IndianState>)
)
