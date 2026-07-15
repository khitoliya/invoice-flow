package com.dollyplastic.invoiceapp.domain.Parsing

import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails

sealed class ComplianceResult {
    data class EWaySuccess(val details: EWayBillDetails) : ComplianceResult()
    data class EInvoiceSuccess(val details: EInvoiceDetails) : ComplianceResult()
    data class Failure(val message: String) : ComplianceResult()
}
