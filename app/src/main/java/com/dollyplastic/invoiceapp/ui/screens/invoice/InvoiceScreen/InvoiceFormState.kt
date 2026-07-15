package com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen

import com.dollyplastic.invoiceapp.data.models.AdditionalDetails
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.data.models.TaxSummary
import com.dollyplastic.invoiceapp.data.models.TransportDetails
import com.dollyplastic.invoiceapp.domain.Utils.DateUtils
import com.dollyplastic.invoiceapp.domain.Utils.FinancialYearUtils

data class InvoiceFormState(

    /* HEADER */
    val isEditing: Boolean = false,
    val invoiceId: String? = null,
    val invoiceNumber: String = "",
    val invoiceSequence: Int = 0,
    val invoiceDate: String = DateUtils.today(),
    val financialYear: String =
        FinancialYearUtils.fromDate(
            DateUtils.parse(invoiceDate)
        ),

    /* ENTITIES */
    val firm: Firm? = null,
    val isCashSale: Boolean = false,
    val billToParty: Party? = null,
    val shipToParty: Party? = null,
    val shipToSameAsBillTo: Boolean = true,

    /* ITEMS */
    val items: List<InvoiceItem> = emptyList(),

    /* TOTALS */
    val taxSummary: TaxSummary = TaxSummary(),
    val totalTaxableValue: Double = 0.0,
    val totalTaxAmount: Double = 0.0,
    val totalInvoiceValue: Double = 0.0,

    /* TRANSPORT */
    val transportDetails: TransportDetails = TransportDetails(),

    /* COMPLIANCE FLAGS */
    val generateEInvoice: Boolean = false,
    val generateEWayBill: Boolean = false,
    val isEWayBillAllowed: Boolean = true,
    val isEInvoiceAllowed: Boolean = true,

    /* VALIDATION */
    val lastInvoiceDateEpoch: Long? = null,
    val errors: Map<String, String> = emptyMap(),
    val internalErrors: Map<String, String> = emptyMap(),


    val additionalDetails: AdditionalDetails = AdditionalDetails(),
    val isSaving: Boolean = false,
    val isDistanceReadOnly: Boolean = false,
    val isGeneratingInvoiceNumber: Boolean = false,
    val touchedFields: Set<String> = emptySet(),
    val showAllErrors: Boolean = false

) {
    fun getVisibleError(field: String): String? {
        return if (showAllErrors || touchedFields.contains(field)) {
            errors[field]
        } else {
            null
        }
    }

    val isFormValid: Boolean
        get() =
            errors.isEmpty() &&
            firm != null &&
            invoiceNumber.isNotBlank() &&
            invoiceSequence > 0 &&
            items.isNotEmpty()

}