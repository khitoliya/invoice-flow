package com.dollyplastic.invoiceapp.domain.Compliance.EwayBill



data class EWayBillDraft(

    /* =========================
       DOCUMENT DETAILS
       ========================= */

    val supplyType: String = "O",          // Outward (FIXED)
    val subSupplyType: Int = 1,             // Supply (FIXED)
    val docType: String = "INV",           // Tax Invoice (FIXED)
    val userGstin: String,
    val subSupplyDesc: String = "",      // OPTIONAL
    val transType : Int,              // Regular (FIXED)

    val docNo: String,                      // invoice.invoiceNumber
    val docDate: String,                   // invoice.invoiceDate (dd/MM/yyyy)


    /* =========================
       SUPPLIER (FROM)
       ========================= */

    val fromGstin: String,                  // invoice.firm.gstin
    val fromTrdName: String,              // invoice.firm.tradeName
    val fromAddr1: String,
    val fromAddr2: String,
    val fromPlace: String,
    val fromPincode: Int,
    val fromStateCode: Int, // invoice.firm.stateCode
    val actualFromStateCode:Int,

    /* =========================
       BUYER / CONSIGNEE (TO)
       Bill-To GSTIN + Ship-To address
       ========================= */

    val toGstin: String,                    // billTo.gstin OR "URP"
    val toTrdName: String,                // billTo.tradeName
    val toAddr1: String,                    // shipTo.addressLine1
    val toAddr2: String,
    val toPlace: String,
    val toPincode: Int,
    val toStateCode: Int,                // shipTo.stateCode
    val actualToStateCode:Int,

    /* =========================
       VALUE DETAILS
       ========================= */

    val totalValue: Double,           // invoice.totaltaxable value
    val cgstValue: Double,                   // invoice.taxSummary.cgst
    val sgstValue: Double,                   // invoice.taxSummary.sgst
    val igstValue: Double,                   // invoice.taxSummary.igst
    val cessValue: Double = 0.0,             // FIXED (unless later needed)
    val TotNonAdvolVal: Double=0.0,
    val OthValue: Double =0.0,
    val totInvValue: Double, // Total Invoice Value

    /* =========================
       TRANSPORT DETAILS (PART-B)
       ========================= */

    val transportMode: Int,   // ROAD / RAIL / AIR / SHIP
    val transDistance: Int,
    val transporterId: String,              // mandatory if transporter
    val transporterName: String,
    val transportDocNo: String,              // mandatory if non-ROAD
    val transportDocDate: String,            // mandatory if non-ROAD

    val vehicleNumber: String,              // mandatory if ROAD
    val vehicleType: String = "R",            // R or O (derived)
    val mainHsnCode: String,





    /* =========================
       ITEM LIST
       ========================= */

    val itemList: List<EWayBillItemDraft>
)

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