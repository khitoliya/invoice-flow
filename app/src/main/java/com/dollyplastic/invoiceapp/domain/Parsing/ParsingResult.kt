package com.dollyplastic.invoiceapp.domain.Parsing

import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails

sealed class ParsingResult {

    data class Success(
        val eInvoiceDetails: EInvoiceDetails? = null,
        val eWayBillDetails: EWayBillDetails? = null
    ) : ParsingResult()

    data class FileError(
        val message: String
    ) : ParsingResult()

    data class DataError(
        val message: String
    ) : ParsingResult()
}
