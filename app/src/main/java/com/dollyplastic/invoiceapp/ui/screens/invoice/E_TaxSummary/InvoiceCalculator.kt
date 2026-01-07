package com.dollyplastic.invoiceapp.ui.screens.invoice.E_TaxSummary

import com.dollyplastic.invoiceapp.data.models.TaxSummary
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceFormState

object InvoiceCalculator {

    fun recalc(state: InvoiceFormState): InvoiceFormState {

        val firm = state.firm ?: return state
        val billTo = state.billToParty

        val isInterState =
            billTo != null && billTo.stateCode != firm.stateCode

        var cgst = 0.0
        var sgst = 0.0
        var igst = 0.0

        val updatedItems = state.items.map { line ->
            val gst = line.taxableValue * line.item.gstRate / 100

            if (isInterState) {
                igst += gst
            } else {
                cgst += gst / 2
                sgst += gst / 2
            }

            line.copy(gstAmount = gst)
        }

        val taxable = updatedItems.sumOf { it.taxableValue }
        val tax = cgst + sgst + igst

        return state.copy(
            items = updatedItems,
            taxSummary = TaxSummary(cgst, sgst, igst),
            totalTaxableValue = taxable,
            totalTaxAmount = tax,
            totalInvoiceValue = taxable + tax
        )
    }
}