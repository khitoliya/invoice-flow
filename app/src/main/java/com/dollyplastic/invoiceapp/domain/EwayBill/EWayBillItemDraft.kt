package com.dollyplastic.invoiceapp.domain.EwayBill

data class EWayBillItemDraft(
    val itemNo: Int,
    val productDesc: String="",
    val productName: String,
    val hsnCode: String,
    val quantity: Double,
    val qtyUnit: String,        // NIC unit code (PCS, KGS, etc.)
    val taxableAmount: Double,
    val cgstRate: Double,
    val sgstRate: Double,
    val igstRate: Double,
    val cessRate: Double=0.0,
    val cessNonAdvol: Double=0.0,
)
