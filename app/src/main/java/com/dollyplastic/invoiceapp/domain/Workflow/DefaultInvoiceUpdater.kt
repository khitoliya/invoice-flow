package com.dollyplastic.invoiceapp.domain.Workflow

import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository

class DefaultInvoiceStatusUpdater(
    private val repository: InvoiceRepository
) : InvoiceStatusUpdater {

    override suspend fun transition(
        invoiceId: String,
        from: InvoiceStatus,
        to: InvoiceStatus,
        reason: String?
    ) {
        if (from == to) {
            android.util.Log.w("InvoiceStatusUpdater", "Ignoring redundant transition $from -> $to for invoice $invoiceId")
            return
        }

        require(InvoiceStatusTransitions.canTransition(from, to)) {
            "Illegal transition: $from → $to"
        }

        repository.updateInvoiceStatus(
            invoiceId = invoiceId,
            status = to,
            error = reason
        )
    }
}
