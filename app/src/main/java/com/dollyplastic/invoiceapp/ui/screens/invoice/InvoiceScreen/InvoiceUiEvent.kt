package com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen

sealed class InvoiceUiEvent {
    data class ShowErrorDialog(val message: String) : InvoiceUiEvent()
    data object ShowConfirmDialog : InvoiceUiEvent()
    data class InvoiceSaved(
        val invoiceId: String,
        val needsProcessing: Boolean
    ) : InvoiceUiEvent()
}