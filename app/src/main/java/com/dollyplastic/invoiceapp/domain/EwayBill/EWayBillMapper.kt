package com.dollyplastic.invoiceapp.domain.EwayBill


import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.TransportMode
import java.time.LocalDate
import java.time.format.DateTimeFormatter


class EWayBillMapper {


    private val NIC_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy")

    private fun mapTransportMode(mode: TransportMode): Int = when (mode) {
            TransportMode.ROAD -> 1
            TransportMode.RAIL -> 2
            TransportMode.AIR  -> 3
            TransportMode.SHIP -> 4
    }
    private fun resolveMainHsnCode(items: List<EWayBillItemDraft>): String {
        return items
            .maxByOrNull { it.taxableAmount }
            ?.hsnCode
            ?: ""
    }
    fun mapInvoiceToEWayBillDraft(
        invoice: Invoice,
        transDistance: Int        // explicitly passed (UI-derived)
    ): EWayBillDraft {
        val isBillToShipToSame = invoice.billToParty?.partyId==invoice.shipToParty?.partyId
        val transType =
            if (invoice.billToParty?.partyId == invoice.shipToParty?.partyId) {
                1 // Regular
            } else {
                2 // Bill To – Ship To
            }

        val firm = invoice.firm
        val billTo = invoice.billToParty
            ?: error("Bill To party is mandatory for e-Way Bill")

        val shipTo = invoice.shipToParty
            ?: error("Ship To party is mandatory for e-Way Bill")

        val transport = invoice.transportDetails

        val docDate = LocalDate
            .parse(invoice.invoiceDate)
            .format(NIC_DATE_FORMAT)

        val transportDocDate =
            if (!transport.transporterDocDate.isNullOrBlank()) {
                LocalDate.parse(transport.transporterDocDate)
                    .format(NIC_DATE_FORMAT)
            } else {
                docDate   // fallback to invoice date
            }


        // ----------------------------
        // ITEM MAPPING
        // ----------------------------
        val itemDrafts = invoice.items.mapIndexed { index, item ->
            EWayBillItemDraft(
                itemNo = index + 1,
                productName = item.item.name,
                hsnCode = item.item.hsnCode,
                quantity = item.quantity,
                qtyUnit = item.item.unit,
                taxableAmount = item.taxableValue,
                cgstRate = if(invoice.firm.stateCode==invoice.billToParty.stateCode) item.item.gstRate/2 else 0.0,
                sgstRate =  if(invoice.firm.stateCode==invoice.billToParty.stateCode) item.item.gstRate/2 else 0.0,
                igstRate = if(invoice.firm.stateCode!=invoice.billToParty.stateCode) item.item.gstRate else 0.0,
            )
        }

        val mainHsn = resolveMainHsnCode(itemDrafts)

        return EWayBillDraft(

            /* =========================
               DOCUMENT DETAILS
               ========================= */

            userGstin = firm.gstin,

            docNo = invoice.invoiceNumber,
            docDate = docDate,
            transType = transType,

            /* =========================
               FROM (SUPPLIER)
               ========================= */

            fromGstin = firm.gstin,
            fromTrdName = firm.tradeName,
            fromAddr1 = firm.addressLine1,
            fromAddr2 = firm.addressLine2 ?: "",
            fromPlace = firm.city,
            fromPincode = firm.pincode.toInt(),
            fromStateCode = firm.stateCode.toInt(),
            actualFromStateCode = firm.stateCode.toInt(),

            /* =========================
               TO (BILL TO + SHIP TO)
               ========================= */

            toGstin = billTo.gstin,
            toTrdName = billTo.tradeName,
            toAddr1 = shipTo.addressLine1,
            toAddr2 = shipTo.addressLine2?:"",
            toPlace = shipTo.city,
            toPincode = shipTo.pincode.toInt(),
            toStateCode = shipTo.stateCode.toInt(),
            actualToStateCode = shipTo.stateCode.toInt(),

            /* =========================
               VALUE DETAILS
               ========================= */

            totalValue = invoice.totalTaxableValue,
            cgstValue = invoice.taxSummary.cgst,
            sgstValue = invoice.taxSummary.sgst,
            igstValue = invoice.taxSummary.igst,
            totInvValue = invoice.totalInvoiceValue,

            /* =========================
               TRANSPORT DETAILS
               ========================= */

            transportMode = mapTransportMode(transport.mode),
            transDistance = transDistance,

            transporterId = transport.transporterId?:"",
            transporterName = transport.transporterName?:"",

            transportDocNo = transport.transporterDocNo?:"",
            transportDocDate = transportDocDate,

            vehicleNumber = transport.vehicleNumber?:"",
            vehicleType =
                if (transport.vehicleType?.name == "ODC") "O" else "R",

            mainHsnCode = mainHsn,

            /* =========================
               ITEMS
               ========================= */

            itemList = itemDrafts
        )
    }



}