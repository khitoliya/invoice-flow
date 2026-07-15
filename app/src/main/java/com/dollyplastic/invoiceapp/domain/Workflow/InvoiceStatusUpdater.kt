package com.dollyplastic.invoiceapp.domain.Workflow

import com.dollyplastic.invoiceapp.data.models.InvoiceStatus

interface InvoiceStatusUpdater {
    suspend fun transition(
        invoiceId: String,
        from: InvoiceStatus,
        to: InvoiceStatus,
        reason: String? = null
    )
}
