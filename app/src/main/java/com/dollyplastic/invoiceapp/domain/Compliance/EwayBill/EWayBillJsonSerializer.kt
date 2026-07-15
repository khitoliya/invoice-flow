package com.dollyplastic.invoiceapp.domain.Compliance.EwayBill

import org.json.JSONArray
import org.json.JSONObject

import javax.inject.Inject

class EWayBillJsonSerializer @Inject constructor() {

    fun serialize(draft: EWayBillDraft): JSONObject {
        val bill = JSONObject()
        val finalRoot = JSONObject()

        bill.put("userGstin", draft.fromGstin)
        bill.put("supplyType", draft.supplyType)
        bill.put("subSupplyType", draft.subSupplyType)
        bill.put("subSupplyDesc", draft.subSupplyDesc)
        bill.put("docType", draft.docType)
        bill.put("docNo", draft.docNo)
        bill.put("docDate", draft.docDate)
        bill.put("transType", draft.transType)

        // FROM
        bill.put("fromGstin", draft.fromGstin)
        bill.put("fromTrdName", draft.fromTrdName)
        bill.put("fromAddr1", draft.fromAddr1)
        bill.put("fromAddr2", draft.fromAddr2)
        bill.put("fromPlace", draft.fromPlace)
        bill.put("fromPincode", draft.fromPincode)
        bill.put("fromStateCode", draft.fromStateCode)
        bill.put("actualFromStateCode", draft.actualFromStateCode)

        // TO
        bill.put("toGstin", draft.toGstin)
        bill.put("toTrdName", draft.toTrdName)
        bill.put("toAddr1", draft.toAddr1)
        bill.put("toAddr2", draft.toAddr2)
        bill.put("toPlace", draft.toPlace)
        bill.put("toPincode", draft.toPincode)
        bill.put("toStateCode", draft.toStateCode)
        bill.put("actualToStateCode", draft.actualToStateCode)

        // VALUE
        bill.put("totalValue", draft.totalValue)
        bill.put("cgstValue", draft.cgstValue)
        bill.put("sgstValue", draft.sgstValue)
        bill.put("igstValue", draft.igstValue)
        bill.put("cessValue", draft.cessValue)
        bill.put("TotNonAdvolVal", draft.TotNonAdvolVal)
        bill.put("OthValue", draft.OthValue)
        bill.put("totInvValue", draft.totInvValue)

        // TRANSPORT
        bill.put("transMode", draft.transportMode)
        bill.put("transDistance", draft.transDistance) // Int
        bill.put("transporterName", draft.transporterName)
        bill.put("transporterId", draft.transporterId)
        bill.put("transDocNo", draft.transportDocNo)
        bill.put("transDocDate", draft.transportDocDate)
        bill.put("vehicleNo", draft.vehicleNumber)
        bill.put("vehicleType", draft.vehicleType)
        
        // HSN (Missing in previous version)
        // User example has it as number/long but usually HSN is string. 
        // Based on user sample "mainHsnCode": 391590 (no quotes), it's numeric.
        // Let's safe-cast to long.
        bill.put("mainHsnCode", draft.mainHsnCode.toLongOrNull() ?: 0)


        // ITEMS
        val itemList = JSONArray()
        draft.itemList.forEach { item ->
            val itemJson = JSONObject()
            itemJson.put("itemNo", item.itemNo) // User sample has itemNo
            itemJson.put("productName", item.productName)
            itemJson.put("productDesc", item.productDesc)
            itemJson.put("hsnCode", item.hsnCode) // User sample has "391590" (quoted string)
            itemJson.put("quantity", item.quantity)
            itemJson.put("qtyUnit", item.qtyUnit)
            itemJson.put("taxableAmount", item.taxableAmount)
            itemJson.put("sgstRate", item.sgstRate)
            itemJson.put("cgstRate", item.cgstRate)
            itemJson.put("igstRate", item.igstRate)
            itemJson.put("cessRate", item.cessRate)
            itemJson.put("cessNonAdvol", item.cessNonAdvol)
            itemList.put(itemJson)
        }

        bill.put("itemList", itemList)
        
        // WRAPPER
        val billLists = JSONArray()
        billLists.put(bill)
        
        finalRoot.put("version", "1.0.0621")
        finalRoot.put("billLists", billLists)

        return finalRoot
    }
}