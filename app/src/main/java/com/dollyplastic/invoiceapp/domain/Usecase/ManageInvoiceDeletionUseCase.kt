package com.dollyplastic.invoiceapp.domain.Usecase

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject
import javax.inject.Singleton
import dagger.hilt.android.qualifiers.ApplicationContext

sealed class DeletionAction {
    object HardDelete : DeletionAction()
    object Archive : DeletionAction()
    object RequirePortalCancellation : DeletionAction()
}

@Singleton
class ManageInvoiceDeletionUseCase @Inject constructor(
    @ApplicationContext private val context: android.content.Context,
    private val repository: InvoiceRepository
) {
    suspend fun analyzeDeletion(invoice: Invoice): DeletionAction {
        // ... (Same logic)
        val sequenceBegun = invoice.status != InvoiceStatus.DRAFT && 
                           invoice.status != InvoiceStatus.PENDING_VALIDATION &&
                           invoice.status != InvoiceStatus.JSON_GENERATION_FAILED &&
                           invoice.status != InvoiceStatus.UPLOAD_FAILED
                           
        val complianceActive = (invoice.generateEInvoice || invoice.generateEWayBill) && sequenceBegun

        if (complianceActive) {
            return DeletionAction.RequirePortalCancellation
        }

        val isLatest = repository.isLatestInvoice(
            invoice.firm.gstin,
            invoice.financialYear,
            invoice.invoiceSequence
        )

        return if (isLatest) {
            DeletionAction.HardDelete
        } else {
            DeletionAction.Archive
        }
    }

    suspend fun executeArchive(invoice: Invoice): Result<Unit> {
        return repository.archiveInvoice(invoice)
    }

    suspend fun executeHardDelete(invoice: Invoice): Result<Unit> {
        // 1. Delete from DB
        val dbResult = repository.deleteInvoice(invoice.invoiceId)
        
        // 2. Delete Files (Best Effort)
        if (dbResult is Result.Success) {
            try {
                com.dollyplastic.invoiceapp.data.repository.InvoiceStorage.deleteTempDirectory(
                    firm = invoice.firm,
                    invoiceNumber = invoice.invoiceNumber
                )
            } catch (e: Exception) {
                // Log but don't fail the operation
                android.util.Log.e("ManageInvoiceDeletion", "Failed to delete directory", e)
            }
        }
        return dbResult
    }
}
