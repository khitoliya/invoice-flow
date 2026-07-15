package com.dollyplastic.invoiceapp.data.models

data class InvoiceStorageRef(
    val firmName: String,
    val financialYear: String,
    val invoiceNumber: String
) {
    fun encode(): String =
        listOf(firmName, financialYear, invoiceNumber)
            .joinToString("|") { java.net.URLEncoder.encode(it, "UTF-8") }

    companion object {
        fun decode(raw: String): InvoiceStorageRef {
            val parts = raw.split("|").map {
                java.net.URLDecoder.decode(it, "UTF-8")
            }
            return InvoiceStorageRef(
                firmName = parts[0],
                financialYear = parts[1],
                invoiceNumber = parts[2]
            )
        }
    }
}


