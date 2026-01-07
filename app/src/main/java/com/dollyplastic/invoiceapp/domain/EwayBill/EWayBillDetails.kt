package com.dollyplastic.invoiceapp.domain.EwayBill

data class EWayBillDetails(
    val ewayBillNo: String ="",
    val ewayBillDate: String="",
    val validUpto: String="",
    val generatedBy: String="",
    val distanceKm: Int=0
)