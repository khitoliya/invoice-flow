package com.dollyplastic.invoiceapp.ui.common.deletion

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Usecase.DeletionAction
import com.dollyplastic.invoiceapp.domain.Usecase.ManageInvoiceDeletionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class DeletionUiState {
    object Idle : DeletionUiState()
    object Processing : DeletionUiState()
    
    // Dialog States
    data class ShowHardDeleteConfirm(val invoice: Invoice) : DeletionUiState()
    data class ShowComplianceWarning(val invoice: Invoice) : DeletionUiState()
    data class ShowCancellationRemark(val invoice: Invoice) : DeletionUiState()
}

class InvoiceDeletionHelper @Inject constructor(
    private val deletionUseCase: ManageInvoiceDeletionUseCase
) {
    private val _uiState = MutableStateFlow<DeletionUiState>(DeletionUiState.Idle)
    val uiState = _uiState.asStateFlow()

    suspend fun onRequestDeletion(invoice: Invoice) {
        _uiState.value = DeletionUiState.Processing
        
        val action = deletionUseCase.analyzeDeletion(invoice)
        
        _uiState.value = when (action) {
            is DeletionAction.HardDelete -> DeletionUiState.ShowHardDeleteConfirm(invoice)
            is DeletionAction.RequirePortalCancellation -> DeletionUiState.ShowComplianceWarning(invoice)
            is DeletionAction.Archive -> DeletionUiState.ShowCancellationRemark(invoice)
        }
    }

    fun dismiss() {
        _uiState.value = DeletionUiState.Idle
    }

    /**
     * User clicked "I have Cancelled" on Warning Dialog.
     * Proceed to Remark Dialog.
     */
    fun onWarningAcknowledged(invoice: Invoice) {
        _uiState.value = DeletionUiState.ShowCancellationRemark(invoice)
    }

    /**
     * Executes Hard Delete. Returns Result for ViewModel to handle (e.g. navigation).
     */
    suspend fun onConfirmHardDelete(invoice: Invoice): Result<Unit> {
        _uiState.value = DeletionUiState.Processing
        val result = deletionUseCase.executeHardDelete(invoice)
        if (result !is Result.Success) {
            _uiState.value = DeletionUiState.Idle // Reset on failure to allow retry
        }
        return result
    }

    /**
     * Executes Archive (Soft Delete). Returns Result for ViewModel to handle.
     */
    suspend fun onConfirmArchive(invoice: Invoice, remark: String): Result<Unit> {
        _uiState.value = DeletionUiState.Processing
        // Note: In future, we can pass 'remark' to useCase if we want to store it in Audit Trail
        val result = deletionUseCase.executeArchive(invoice)
         if (result !is Result.Success) {
            _uiState.value = DeletionUiState.Idle
        }
        return result
    }
}
