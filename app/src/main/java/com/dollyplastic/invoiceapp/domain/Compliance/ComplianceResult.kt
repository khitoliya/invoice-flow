package com.dollyplastic.invoiceapp.domain.Compliance

import java.io.File

sealed class ComplianceResult {

    data class ReadyForPortal(
        val portalUrl: String,
        val jsonFile: File
    ) : ComplianceResult()

    object PdfOnly : ComplianceResult()

    data class Error(
        val message: String
    ) : ComplianceResult()
}
