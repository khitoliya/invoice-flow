package com.dollyplastic.invoiceapp.data.models

data class Firm(
    val firmId: String="",            // internal unique ID
    val tradeName: String="",         // PRIMARY name (invoice display)
    val nickName: String="",          // short / internal identifier

    val gstin: String="",

    val addressLine1: String="",
    val addressLine2: String?=null,
    val city: String="",
    val state: String="",
    val stateCode: String="",
    val pincode: String="",
    //Bank details
    val bankName: String="",
    val accountNumber: String="",
    val ifscCode: String="",
    val branchName: String=""
)
