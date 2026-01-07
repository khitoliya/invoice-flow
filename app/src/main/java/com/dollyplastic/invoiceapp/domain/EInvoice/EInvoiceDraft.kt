package com.dollyplastic.invoiceapp.domain.EInvoice

import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillItemDraft

data class EInvoiceDraft(
    val Version: String = "1.1",
    val TranDtls: TranDtls,
    val DocDtls: DocDtls,
    val SellerDtls: SellerDtls,
    val BuyerDtls: BuyerDtls,
    val DispDtls: DispDtls?,
    val ShipDtls: ShipDtls?,
    val ItemList: List<EInvoiceItem>,
    val ValDtls: ValDtls,
    val PayDtls: PayDtls?,
    val RefDtls: RefDtls?,
    val AddlDocDtls: List<AddlDocDtls>?,
    val ExpDtls: ExpDtls?,
    val EwbDtls: EwbDtls? // E-Way Bill Details (if generating both)
)

data class TranDtls(
    val TaxSch: String = "GST",
    val SupTyp: String, // B2B, SEZWP, SEZWOP, EXPWP, EXPWOP, DEXP
    val RegRev: String = "N", // Y/N (Reverse Charge)
    val EcmGstin: String? = null,
    val IgstOnIntra: String = "N"
)

data class DocDtls(
    val Typ: String = "INV", // INV, CRN, DBN
    val No: String,
    val Dt: String // dd/mm/yyyy
)

data class SellerDtls(
    val Gstin: String,
    val LglNm: String,
    val TrdNm: String?,
    val Addr1: String,
    val Addr2: String?,
    val Loc: String,
    val Pin: Int,
    val Stcd: String,
    val Ph: String? = null,
    val Em: String? = null
)

data class BuyerDtls(
    val Gstin: String,
    val LglNm: String,
    val TrdNm: String?,
    val Pos: String, // State Code
    val Addr1: String,
    val Addr2: String?,
    val Loc: String,
    val Pin: Int,
    val Stcd: String,
    val Ph: String? = null,
    val Em: String? = null
)

data class DispDtls(
    val Nm: String?,
    val Addr1: String,
    val Addr2: String?,
    val Loc: String,
    val Pin: Int,
    val Stcd: String
)

data class ShipDtls(
    val Gstin: String?, // URP if unregistered
    val LglNm: String?,
    val TrdNm: String?,
    val Addr1: String,
    val Addr2: String?,
    val Loc: String,
    val Pin: Int,
    val Stcd: String
)

data class EInvoiceItem(
    val SlNo: String,
    val PrdDesc: String?,
    val IsServc: String = "N", // Y/N
    val HsnCd: String,
    val Barcde: String? = null,
    val Qty: Double,
    val FreeQty: Double = 0.0,
    val Unit: String,
    val UnitPrice: Double,
    val TotAmt: Double,
    val Discount: Double = 0.0,
    val PreTaxVal: Double?, // optional? usually derived
    val AssVal: Double, // Taxable Value
    val GstRt: Double,
    val IgstAmt: Double = 0.0,
    val CgstAmt: Double = 0.0,
    val SgstAmt: Double = 0.0,
    val CesRt: Double = 0.0,
    val CesAmt: Double = 0.0,
    val CesNonAdvlAmt: Double = 0.0,
    val StateCesRt: Double = 0.0,
    val StateCesAmt: Double = 0.0,
    val StateCesNonAdvlAmt: Double = 0.0,
    val OthChrg: Double = 0.0,
    val TotItemVal: Double
)

data class ValDtls(
    val AssVal: Double,
    val CgstVal: Double,
    val SgstVal: Double,
    val IgstVal: Double,
    val CesVal: Double,
    val StCesVal: Double,
    val Discount: Double = 0.0,
    val OthChrg: Double = 0.0,
    val RndOffAmt: Double,
    val TotInvVal: Double,
    val TotInvValFc: Double? = null
)

data class PayDtls(
    val Nm: String?,
    val AccDet: String?,
    val Mode: String?,
    val FinInsBr: String?,
    val PayTerm: String?,
    val PayInstr: String?,
    val CrTrn: String?,
    val DirDr: String?,
    val CrDay: Int?,
    val PaidAmt: Double?,
    val PaymtDue: Double?
)

data class RefDtls(
    val InvRm: String?,
    val DocPerdDtls: DocPerdDtls?,
    val PrecDocDtls: List<PrecDocDtls>?,
    val ContrDtls: List<ContrDtls>?
)

data class DocPerdDtls(val InvStDt: String, val InvEndDt: String)
data class PrecDocDtls(val InvNo: String, val InvDt: String, val OthRefNo: String?)
data class ContrDtls(val RecAdvRefr: String?, val RecAdvDt: String?, val TendRefr: String?, val ContrRefr: String?, val ExtRefr: String?, val ProjRefr: String?, val PORefr: String?, val PORefDt: String?)

data class AddlDocDtls(val Url: String, val Docs: String, val Info: String?)
data class ExpDtls(val ShipBNo: String?, val ShipBDt: String?, val Port: String?, val RefClm: String?, val ForCur: String?, val CntCode: String?)

data class EwbDtls(
    val TransId: String?,
    val TransName: String?,
    val TransMode: String?, // 1,2,3,4 as string?
    val Distance: Int,
    val TransDocNo: String?,
    val TransDocDt: String?,
    val VehNo: String?,
    val VehType: String?
)
