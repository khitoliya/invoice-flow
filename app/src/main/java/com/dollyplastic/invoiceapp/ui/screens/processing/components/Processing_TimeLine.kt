package com.dollyplastic.invoiceapp.ui.screens.processing.components

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import javax.inject.Inject


object TimelineStepId {
    const val PREPARE = "prepare"
    const val PORTAL = "portal"
    const val FINALIZE = "finalize"
    const val READY = "ready"
}


data class TimelineStep(
    val id: String,
    val title: String,
    val state: StepState,
    val message: String? = null,
    val primaryAction: TimelineAction? = null,
    val secondaryAction: TimelineAction? = null
)
private data class StepConfig(
    val state: StepState,
    val message: String? = null,
    val primary: TimelineAction? = null,
    val secondary: TimelineAction? = null
)


enum class StepState {
    PENDING,
    ACTIVE,
    COMPLETED,
    FAILED
}

data class TimelineAction(
    val label: String,
    val action: TimelineActionType
)

enum class TimelineActionType {
    RETRY_JSON,
    OPEN_PORTAL,
    RETRY_UPLOAD,
    RETRY_PARSING,
    REDOWNLOAD_RESULT,
    VIEW_PDF,
    SHARE_PDF,
    EDIT_INVOICE
}




class InvoiceTimelineMapper @Inject constructor() {

    fun map(invoice: Invoice): List<TimelineStep> {

        // PDF-only shortcut
        if (!invoice.generateEInvoice && !invoice.generateEWayBill) {
            return listOf(
                TimelineStep(
                    id = TimelineStepId.PREPARE,
                    title = "Prepare Data",
                    state = StepState.COMPLETED
                ),
                TimelineStep(
                    id = TimelineStepId.READY,
                    title = "Ready",
                    state = StepState.COMPLETED,
                    primaryAction = TimelineAction(
                        "View PDF",
                        TimelineActionType.VIEW_PDF
                    ),
                    secondaryAction = TimelineAction(
                        "Share",
                        TimelineActionType.SHARE_PDF
                    )
                )
            )
        }

        val prepare = when (invoice.status) {
            InvoiceStatus.GENERATING_JSON ->
                StepConfig(StepState.ACTIVE)

            InvoiceStatus.JSON_GENERATION_FAILED -> {
                val error = invoice.processingError ?: "Unknown Error"
                val prefix = if (error.contains("Validation", true)) "Validation Error:" else "System Error (JSON Generation):"
                
                StepConfig(
                    state = StepState.FAILED,
                    message = "$prefix\n$error",
                    primary = TimelineAction("Retry JSON", TimelineActionType.RETRY_JSON),
                    secondary = TimelineAction("Edit Invoice", TimelineActionType.EDIT_INVOICE)
                )
            }

            InvoiceStatus.WAITING_FOR_UPLOAD,
            InvoiceStatus.PROCESSING_RESULT,
            InvoiceStatus.PARSING_FAILED,
            InvoiceStatus.COMPLETED ->
                StepConfig(StepState.COMPLETED)

            else ->
                // Default / DRAFT state: Show "Generate JSON" button (Manual Start Fallback)
                StepConfig(
                    state = StepState.ACTIVE,
                    primary = TimelineAction("Generate JSON", TimelineActionType.RETRY_JSON),
                    secondary = TimelineAction("Edit Invoice", TimelineActionType.EDIT_INVOICE)
                )
        }

        val portal = when (invoice.status) {
            InvoiceStatus.WAITING_FOR_UPLOAD ->
                StepConfig(
                    state = StepState.ACTIVE,
                    primary = TimelineAction("Open Portal", TimelineActionType.OPEN_PORTAL)
                )

            InvoiceStatus.UPLOAD_FAILED ->
                StepConfig(
                    state = StepState.FAILED,
                    message = invoice.processingError,
                    primary = TimelineAction("Retry Upload", TimelineActionType.RETRY_UPLOAD),
                    secondary = TimelineAction("Edit Invoice", TimelineActionType.EDIT_INVOICE)
                )

            InvoiceStatus.PROCESSING_RESULT,
            InvoiceStatus.PARSING_FAILED,
            InvoiceStatus.COMPLETED ->
                StepConfig(StepState.COMPLETED)

            else -> StepConfig(StepState.PENDING)
        }

        val finalize = when (invoice.status) {
            InvoiceStatus.PROCESSING_RESULT ->
                StepConfig(StepState.ACTIVE)

            InvoiceStatus.PARSING_FAILED ->
                StepConfig(
                    state = StepState.FAILED,
                    message = invoice.processingError,
                    primary = TimelineAction("Retry Parsing", TimelineActionType.RETRY_PARSING),
                    secondary = TimelineAction("Re-download", TimelineActionType.REDOWNLOAD_RESULT)
                )

            InvoiceStatus.COMPLETED ->
                StepConfig(StepState.COMPLETED)

            else -> StepConfig(StepState.PENDING)
        }

        val ready = when (invoice.status) {
            InvoiceStatus.COMPLETED ->
                StepConfig(
                    state = StepState.COMPLETED,
                    primary = TimelineAction("View PDF", TimelineActionType.VIEW_PDF),
                    secondary = TimelineAction("Share", TimelineActionType.SHARE_PDF)
                )

            else -> StepConfig(StepState.PENDING)
        }

        return listOf(
            build(TimelineStepId.PREPARE, "Prepare Data", prepare),
            build(TimelineStepId.PORTAL, "Government Portal", portal),
            build(TimelineStepId.FINALIZE, "Finalize", finalize),
            build(TimelineStepId.READY, "Ready", ready)
        )
    }

    private fun build(
        id: String,
        title: String,
        cfg: StepConfig
    ): TimelineStep =
        TimelineStep(
            id = id,
            title = title,
            state = cfg.state,
            message = cfg.message,
            primaryAction = cfg.primary,
            secondaryAction = cfg.secondary
        )
}



