package com.dollyplastic.invoiceapp.domain.EwayBill

import org.json.JSONArray
import org.json.JSONObject

class EWayBillJsonSerializer {

    fun serialize(draft: EWayBillDraft): JSONObject {
        val root = JSONObject()

        root.put("supplyType", draft.supplyType)
        root.put("subSupplyType", draft.subSupplyType)
        root.put("subSupplyDesc", draft.subSupplyDesc)
        root.put("docType", draft.docType)
        root.put("docNo", draft.docNo)
        root.put("docDate", draft.docDate)
        root.put("transType", draft.transType)

        // FROM
        root.put("fromGstin", draft.fromGstin)
        root.put("fromTrdName", draft.fromTrdName)
        root.put("fromAddr1", draft.fromAddr1)
        root.put("fromAddr2", draft.fromAddr2)
        root.put("fromPlace", draft.fromPlace)
        root.put("fromPincode", draft.fromPincode)
        root.put("fromStateCode", draft.fromStateCode)
        root.put("actFromStateCode", draft.actualFromStateCode)

        // TO
        root.put("toGstin", draft.toGstin)
        root.put("toTrdName", draft.toTrdName)
        root.put("toAddr1", draft.toAddr1)
        root.put("toAddr2", draft.toAddr2)
        root.put("toPlace", draft.toPlace)
        root.put("toPincode", draft.toPincode)
        root.put("toStateCode", draft.toStateCode)
        root.put("actToStateCode", draft.actualToStateCode)

        // VALUE
        root.put("totalValue", draft.totalValue)
        root.put("cgstValue", draft.cgstValue)
        root.put("sgstValue", draft.sgstValue)
        root.put("igstValue", draft.igstValue)
        root.put("cessValue", draft.cessValue)
        root.put("totInvValue", draft.totInvValue)

        // TRANSPORT
        root.put("transMode", draft.transportMode)
        root.put("transDistance", draft.transDistance.toString()) // API expects String mostly? or Int? Keeping string to be safe based on some schemas, but draft has Int. Let's use string as per some NIC examples.
        root.put("transporterName", draft.transporterName)
        root.put("transporterId", draft.transporterId)
        root.put("transDocNo", draft.transportDocNo)
        root.put("transDocDate", draft.transportDocDate)
        root.put("vehicleNo", draft.vehicleNumber)
        root.put("vehicleType", draft.vehicleType)

        // ITEMS
        val itemList = JSONArray()
        draft.itemList.forEach { item ->
            val itemJson = JSONObject()
            itemJson.put("productName", item.productName)
            itemJson.put("productDesc", item.productDesc)
            itemJson.put("hsnCode", item.hsnCode.toLongOrNull() ?: 0) // HSN often numeric
            itemJson.put("quantity", item.quantity)
            itemJson.put("qtyUnit", item.qtyUnit)
            itemJson.put("taxableAmount", item.taxableAmount)
            itemJson.put("sgstRate", item.sgstRate)
            itemJson.put("cgstRate", item.cgstRate)
            itemJson.put("igstRate", item.igstRate)
            itemJson.put("cessRate", item.cessRate)
            itemList.put(itemJson)
        }

        root.put("itemList", itemList)

        return root
    }
}