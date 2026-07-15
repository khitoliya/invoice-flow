package com.dollyplastic.invoiceapp.domain.Compliance.EInvoice

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.json.JSONObject

import javax.inject.Inject

class EInvoiceJsonSerializer @Inject constructor() {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun serialize(draft: EInvoiceDraft): JSONObject {
        // Gson → String
        val jsonString = gson.toJson(draft)

        // String → JSONObject (single conversion point)
        return JSONObject(jsonString)
    }
}
