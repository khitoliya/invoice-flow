package com.dollyplastic.invoiceapp.domain.Workflow

import com.dollyplastic.invoiceapp.data.models.InvoiceStatus

object InvoiceStatusTransitions {

    private val allowed = mapOf(
        InvoiceStatus.DRAFT to setOf(InvoiceStatus.GENERATING_JSON, InvoiceStatus.PREVIEW_CONFIRMED, InvoiceStatus.CANCELLED, InvoiceStatus.COMPLETED),
        InvoiceStatus.PREVIEW_CONFIRMED to setOf(
            InvoiceStatus.DRAFT,
            InvoiceStatus.GENERATING_JSON,
            InvoiceStatus.CANCELLED,
            InvoiceStatus.COMPLETED
        ),
        InvoiceStatus.GENERATING_JSON to setOf(
            InvoiceStatus.WAITING_FOR_UPLOAD,
            InvoiceStatus.JSON_GENERATION_FAILED
        ),
        InvoiceStatus.WAITING_FOR_UPLOAD to setOf(
            InvoiceStatus.PROCESSING_RESULT,
            InvoiceStatus.UPLOAD_FAILED,
            InvoiceStatus.GENERATING_JSON // Allowed for regeneration if file is missing
        ),
        InvoiceStatus.PROCESSING_RESULT to setOf(
            InvoiceStatus.COMPLETED,
            InvoiceStatus.PARSING_FAILED
        ),
        InvoiceStatus.JSON_GENERATION_FAILED to setOf(
            InvoiceStatus.GENERATING_JSON,
            InvoiceStatus.CANCELLED
        ),
        InvoiceStatus.UPLOAD_FAILED to setOf(
            InvoiceStatus.WAITING_FOR_UPLOAD,
            InvoiceStatus.CANCELLED
        ),
        InvoiceStatus.PARSING_FAILED to setOf(
            InvoiceStatus.PROCESSING_RESULT,
            InvoiceStatus.WAITING_FOR_UPLOAD,
            InvoiceStatus.CANCELLED
        )
    )

    fun canTransition(from: InvoiceStatus, to: InvoiceStatus): Boolean =
        allowed[from]?.contains(to) == true
}
