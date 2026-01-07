package com.dollyplastic.invoiceapp.data.models

enum class DeliveryType {
    OWN_VEHICLE,
    TRANSPORTER,
    BUYER_PICKUP
}

enum class TransportMode {
    ROAD,
    RAIL,
    AIR,
    SHIP
}
enum class VehicleType {
    REGULAR,
    ODC
}

data class TransportDetails(

    /* ---------- PRIMARY ---------- */

    val deliveryType: DeliveryType = DeliveryType.OWN_VEHICLE,
    val mode: TransportMode = TransportMode.ROAD,

    /* ---------- ROAD ---------- */

    val vehicleNumber: String? = null,
    val vehicleType: VehicleType? = null,

    /* ---------- TRANSPORTER ---------- */

    val transporterName: String? = null,
    val transporterId: String? = null,

    /* ---------- DOCUMENT ---------- */

    val transporterDocNo: String? = null,     // LR / RR / AWB / BL
    val transporterDocDate: String? = null,   // dd-MM-yyyy
    val distance: Int = 0 // Required for E-Way Bill

    /* ---------- OPTIONAL ---------- */

    val portOfLoading: String? = null,
    val portOfDischarge: String? = null,
)
