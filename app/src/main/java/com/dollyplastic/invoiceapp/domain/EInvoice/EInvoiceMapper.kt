package com.dollyplastic.invoiceapp.domain.EInvoice

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.TransportMode
import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillMapper
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

class EInvoiceMapper {

    private val DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    fun mapInvoiceToEInvoiceDraft(
        invoice: Invoice,
        distance: Int = 0 // Required if E-Way bill details are included
    ): EInvoiceDraft {
        
        val firm = invoice.firm
        val billTo = invoice.billToParty!! // Helper assumes validated
        val shipTo = invoice.shipToParty ?: billTo // Fallback to BillTo if null (Regular flow)
        
        // Supply Type Determination (Simplified)
        // B2B: Registered to Registered
        // B2C: Registered to Unregistered (Actually B2C not allowed for E-Invoice usually, but let's assume B2B for now or handle appropriately)
        val supplyType = "B2B" 

        val isIntraState = firm.stateCode == billTo.stateCode
        
        return EInvoiceDraft(
            TranDtls = TranDtls(
                SupTyp = supplyType
            ),
            DocDtls = DocDtls(
                No = invoice.invoiceNumber,
                Dt = LocalDate.parse(invoice.invoiceDate).format(DATE_FORMAT)
            ),
            SellerDtls = SellerDtls(
                Gstin = firm.gstin,
                LglNm = firm.tradeName, // Assuming Trade name is Legal name for now
                TrdNm = firm.tradeName,
                Addr1 = firm.addressLine1,
                Addr2 = firm.addressLine2,
                Loc = firm.city,
                Pin = firm.pincode.toIntOrNull() ?: 0,
                Stcd = firm.stateCode
            ),
            BuyerDtls = BuyerDtls(
                Gstin = billTo.gstin,
                LglNm = billTo.tradeName,
                TrdNm = billTo.nickName.ifBlank { billTo.tradeName },
                Pos = shipTo.stateCode, // Place of Supply is usually Ship To State
                Addr1 = billTo.addressLine1,
                Addr2 = billTo.addressLine2,
                Loc = billTo.city,
                Pin = billTo.pincode.toIntOrNull() ?: 0,
                Stcd = billTo.stateCode
            ),
            // DispDtls = Dispatch from (Seller), optional if same as Seller
            DispDtls = null, 
            
            // ShipDtls = Ship To (Consignee)
            ShipDtls = if (invoice.shipToParty != null && invoice.billToParty?.partyId != invoice.shipToParty?.partyId) {
                ShipDtls(
                    Gstin = shipTo.gstin.ifBlank { "URP" },
                    LglNm = shipTo.tradeName,
                    TrdNm = shipTo.tradeName,
                    Addr1 = shipTo.addressLine1,
                    Addr2 = shipTo.addressLine2,
                    Loc = shipTo.city,
                    Pin = shipTo.pincode.toIntOrNull() ?: 0,
                    Stcd = shipTo.stateCode
                )
            } else null,

            ItemList = invoice.items.mapIndexed { index, item ->
                EInvoiceItem(
                    SlNo = (index + 1).toString(),
                    PrdDesc = item.item.name,
                    HsnCd = item.item.hsnCode,
                    Qty = item.quantity,
                    Unit = item.item.unit,
                    UnitPrice = item.rate,
                    TotAmt = item.taxableValue, // Gross Amount (Qty * Rate) - Discount?
                    AssVal = item.taxableValue,
                    GstRt = item.item.gstRate,
                    CgstAmt = if (isIntraState) (item.taxableValue * item.item.gstRate / 2 / 100) else 0.0,
                    SgstAmt = if (isIntraState) (item.taxableValue * item.item.gstRate / 2 / 100) else 0.0,
                    IgstAmt = if (!isIntraState) (item.taxableValue * item.item.gstRate / 100) else 0.0,
                    TotItemVal = item.taxableValue + (item.taxableValue * item.item.gstRate / 100)
                )
            },
            ValDtls = ValDtls(
                AssVal = invoice.totalTaxableValue,
                CgstVal = invoice.taxSummary.cgst,
                SgstVal = invoice.taxSummary.sgst,
                IgstVal = invoice.taxSummary.igst,
                CesVal = 0.0,
                StCesVal = 0.0,
                RndOffAmt = 0.0, // Calculate if needed
                TotInvVal = invoice.totalInvoiceValue
            ),
            PayDtls = null, // Optional
            RefDtls = null, // Optional
            AddlDocDtls = null,
            ExpDtls = null,
            
            // Generate EWB info ONLY if requested
            EwbDtls = if (invoice.generateEWayBill) {
                EwbDtls(
                    TransId = invoice.transportDetails.transporterId,
                    TransName = invoice.transportDetails.transporterName,
                    Distance = distance,
                    TransDocNo = invoice.transportDetails.transporterDocNo,
                    TransDocDt = invoice.transportDetails.transporterDocDate?.let { 
                         // Ensure format? 
                         LocalDate.parse(it).format(DATE_FORMAT) 
                    },
                    VehNo = invoice.transportDetails.vehicleNumber,
                    VehType = invoice.transportDetails.vehicleType?.let { if(it.name=="ODC") "O" else "R" },
                    TransMode = when(invoice.transportDetails.mode) {
                        TransportMode.ROAD -> "1"
                        TransportMode.RAIL -> "2"
                        TransportMode.AIR -> "3"
                        TransportMode.SHIP -> "4"
                    }
                )
            } else null
        )
    }
}
