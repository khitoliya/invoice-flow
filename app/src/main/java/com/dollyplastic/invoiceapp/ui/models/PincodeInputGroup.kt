package com.dollyplastic.invoiceapp.ui.models

data class PincodeInputGroup(
    val pincode: String,
    val entityNames: List<String>,
    var distance: String = ""
)
