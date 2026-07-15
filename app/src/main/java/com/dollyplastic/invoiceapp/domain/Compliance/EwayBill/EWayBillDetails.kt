package com.dollyplastic.invoiceapp.domain.Compliance.EwayBill

data class EWayBillDetails(
    val ewayBillNo: String ="",
    val ewayBillDate: String="",
    val validUpto: String="",
    val generatedBy: String="",
    val validFrom: String="",
    val distanceKm: Int=0
)