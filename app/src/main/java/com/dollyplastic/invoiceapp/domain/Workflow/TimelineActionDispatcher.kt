package com.dollyplastic.invoiceapp.domain.Workflow

import com.dollyplastic.invoiceapp.ui.screens.processing.components.TimelineActionType

interface TimelineActionDispatcher {
    fun onAction(action: TimelineActionType)
}
