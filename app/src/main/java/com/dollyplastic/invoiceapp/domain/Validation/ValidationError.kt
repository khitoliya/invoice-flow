package com.dollyplastic.invoiceapp.domain.Validation

data class ValidationError(
    val field: String,          // e.g. "gstin", "vehicleNumber"
    val message: String,        // human-readable
    val section: String,        // e.g. "Firm", "Party", "Transport"
    val severity: Severity = Severity.ERROR
)

enum class Severity {
    ERROR,      // blocks action
    WARNING     // informational (future use)
}
