package com.dollyplastic.invoiceapp.data.utils

import com.dollyplastic.invoiceapp.data.models.Invoice

object PortalUtils {
    const val URL_E_INVOICE = "https://einvoice1.gst.gov.in/"
    const val URL_E_WAY_BILL = "https://ewaybillgst.gov.in/MainMenu.aspx"

    const val MODE_E_INVOICE = "EINVOICE"
    const val MODE_E_WAY = "EWAY"
    const val MODE_BOTH = "EINVOICE_AND_EWAY"

    fun getPortalUrl(invoice: Invoice): String {
        return when {
            invoice.generateEInvoice -> URL_E_INVOICE
            invoice.generateEWayBill -> URL_E_WAY_BILL
            else -> URL_E_WAY_BILL
        }
    }

    fun getPortalMode(invoice: Invoice, url: String? = null): String {
        val targetUrl = url ?: getPortalUrl(invoice)
        // Simple heuristic: if URL contains "einvoice", it's E-Invoice mode
        return if (targetUrl.contains("einvoice", ignoreCase = true)) {
            if (invoice.generateEInvoice && invoice.generateEWayBill) MODE_BOTH else MODE_E_INVOICE
        } else {
            MODE_E_WAY
        }
    }
}
