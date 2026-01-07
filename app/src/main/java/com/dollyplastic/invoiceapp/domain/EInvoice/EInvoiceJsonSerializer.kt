package com.dollyplastic.invoiceapp.domain.EInvoice

import com.google.gson.Gson
import com.google.gson.GsonBuilder

class EInvoiceJsonSerializer {

    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun serialize(draft: EInvoiceDraft): String {
        return gson.toJson(draft)
    }
}
