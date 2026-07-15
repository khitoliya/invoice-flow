package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.InvoiceDao
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.sync.SyncManager
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class OfflineFirstInvoiceRepository @Inject constructor(
    private val invoiceDao: InvoiceDao,
    private val firestoreDataSource: FirestoreInvoiceDataSource,
    private val syncManager: SyncManager
) : InvoiceRepository {

    private val TAG = "OfflineFirstSync"


    init {
        // Migration Logic: Backfill invoiceDateEpoch and invoiceSequence if missing
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val all = invoiceDao.getAllInvoices()
                val toUpdate = all.filter { (it.invoiceDateEpoch == 0L && it.invoiceDate.isNotBlank()) || it.invoiceSequence == 0 }
                
                if (toUpdate.isNotEmpty()) {
                    android.util.Log.i(TAG, "Migrating ${toUpdate.size} invoices (Date/Sequence)...")
                    toUpdate.forEach { inv ->
                        try {
                            var updated = inv
                            
                            // 1. Backfill Date Epoch
                            if (updated.invoiceDateEpoch == 0L) {
                                val epoch = parseDateToEpoch(updated.invoiceDate)
                                if (epoch != 0L) updated = updated.copy(invoiceDateEpoch = epoch)
                            }
                            
                            // 2. Backfill Sequence (Extract from "DOC/24-25/123" -> 123)
                            if (updated.invoiceSequence == 0) {
                                val parts = updated.invoiceNumber.split("/")
                                val lastPart = parts.lastOrNull()
                                val seq = lastPart?.toIntOrNull()
                                if (seq != null) {
                                     updated = updated.copy(invoiceSequence = seq)
                                }
                            }
                            
                            if (updated != inv) {
                                invoiceDao.update(updated)
                            }
                        } catch (e: Exception) {
                            android.util.Log.e(TAG, "Failed to migrate invoice ${inv.invoiceNumber}", e)
                        }
                    }
                    android.util.Log.i(TAG, "Migration Complete.")
                }
            } catch (e: Exception) {
                android.util.Log.e(TAG, "Migration Failed", e)
            }
        }
    }

    private fun parseDateToEpoch(dateStr: String): Long {
        if (dateStr.isBlank()) return 0L
        val formats = listOf("dd/MM/yyyy", "dd-MM-yyyy", "yyyy-MM-dd", "yyyyMMdd")
        for (f in formats) {
            try {
                val date = java.text.SimpleDateFormat(f, java.util.Locale.US).parse(dateStr)
                if (date != null) return date.time
            } catch (e: Exception) {
                // Ignore and try next
            }
        }
        return 0L
    }

    override suspend fun createInvoice(invoice: Invoice): Result<Unit> {
        try {
            val epoch = parseDateToEpoch(invoice.invoiceDate)
            val safeInvoice = invoice.copy(
                firmGstin = invoice.firm.gstin,
                updatedAt = System.currentTimeMillis(),
                invoiceDateEpoch = epoch
            )
            android.util.Log.d(TAG, "[InvoiceRepo] Saving Invoice Locally: ${safeInvoice.invoiceNumber}")
            invoiceDao.insert(safeInvoice)

            // Push to Remote IMMEDIATELY to reserve the sequence number and prevent duplicates.
            android.util.Log.d(TAG, "[InvoiceRepo] Saving to DB and Triggering Push (Status: ${safeInvoice.status})")
            syncManager.pushInvoice(safeInvoice)
            return Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[InvoiceRepo] Failed to create invoice", e)
            return Result.Error(e)
        }
    }

    override suspend fun updateInvoice(invoice: Invoice): Result<Unit> {
        return try {
            val epoch = parseDateToEpoch(invoice.invoiceDate)
            val safeInvoice = invoice.copy(
                updatedAt = System.currentTimeMillis(),
                invoiceDateEpoch = epoch
            )
            android.util.Log.d(TAG, "[InvoiceRepo] Updating Invoice Locally: ${safeInvoice.invoiceNumber}")
            invoiceDao.insert(safeInvoice)
            syncManager.pushInvoice(safeInvoice)
            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[InvoiceRepo] Failed to update invoice", e)
            Result.Error(e)
        }
    }

    override suspend fun getInvoice(invoiceId: String): Result<Invoice> {
        val local = invoiceDao.getInvoice(invoiceId)
        return if (local != null) {
            android.util.Log.d(TAG, "[InvoiceRepo] getInvoice: Found Locally")
            Result.Success(local)
        } else {
            android.util.Log.w(TAG, "[InvoiceRepo] getInvoice: Not found locally, trying Sync-on-Miss")
            val syncResult = syncManager.syncInvoice(invoiceId)
             if (syncResult is Result.Success) {
                 val newLocal = invoiceDao.getInvoice(invoiceId)
                 if (newLocal != null) {
                     android.util.Log.d(TAG, "[InvoiceRepo] getInvoice: Found after Sync")
                     Result.Success(newLocal)
                 } else {
                     android.util.Log.e(TAG, "[InvoiceRepo] getInvoice: Still missing after sync success!")
                     Result.Error(Exception("Invoice not found even after sync"))
                 }
             } else {
                 android.util.Log.e(TAG, "[InvoiceRepo] getInvoice: Sync failed")
                 Result.Error(Exception("Invoice not found locally and sync failed"))
             }
        }
    }

    override suspend fun getAllInvoices(): Result<List<Invoice>> {
        android.util.Log.d(TAG, "[InvoiceRepo] getAllInvoices: Reading Local DB")
        return Result.Success(invoiceDao.getAllInvoices())
    }

    override fun observeFilteredInvoices(firmGstin: String?, query: String?): Flow<List<Invoice>> {
        return invoiceDao.observeFilteredInvoices(firmGstin, query)
    }

    override suspend fun getInvoicesPaged(
        firmGstin: String?,
        query: String?,
        partyId: String?,
        status: InvoiceStatus?,
        minAmount: Double?,
        maxAmount: Double?,
        hsnCode: String?,
        startEpoch: Long?,
        endEpoch: Long?,
        limit: Int,
        offset: Int
    ): Result<List<Invoice>> {
        return try {
            val invoices = invoiceDao.getInvoicesPaged(
                firmGstin = firmGstin,
                query = query,
                partyId = partyId,
                status = status?.name,
                minAmount = minAmount,
                maxAmount = maxAmount,
                hsnCode = hsnCode,
                startEpoch = startEpoch,
                endEpoch = endEpoch,
                limit = limit,
                offset = offset
            )
            android.util.Log.d("OfflineFirstSync", "[Repo] getInvoicesPaged: Found ${invoices.size} locally. (Offset=$offset)")
            if (invoices.isEmpty() && offset == 0) {
                 android.util.Log.w("OfflineFirstSync", "[Repo] Local DB is empty for this filter! This might explain missing data if Sync hasn't run.")
            }
            Result.Success(invoices)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
    
    override suspend fun getFilteredSummary(
        firmGstin: String?,
        query: String?,
        partyId: String?,
        status: InvoiceStatus?,
        minAmount: Double?,
        maxAmount: Double?,
        hsnCode: String?,
        startEpoch: Long?,
        endEpoch: Long?
    ): Result<Pair<Int, Double>> {
         return try {
            val summary = invoiceDao.getFilteredInvoiceSummary(
                firmGstin = firmGstin,
                query = query,
                partyId = partyId,
                status = status?.name,
                minAmount = minAmount,
                maxAmount = maxAmount,
                hsnCode = hsnCode,
                startEpoch = startEpoch,
                endEpoch = endEpoch
            )
            Result.Success(summary.count to (summary.totalValue ?: 0.0))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun observeFinancialYears(): Flow<List<String>> {
        return invoiceDao.observeFinancialYears()
    }

    override suspend fun existsById(invoiceId: String): Boolean {
        if (invoiceDao.getInvoice(invoiceId) != null) {
            android.util.Log.d(TAG, "[InvoiceRepo] existsById: True (Local)")
            return true
        }
        android.util.Log.d(TAG, "[InvoiceRepo] existsById: False (Local), checking Remote")
        val remoteExists = firestoreDataSource.existsById(invoiceId)
        if (remoteExists) {
             android.util.Log.d(TAG, "[InvoiceRepo] existsById: True (Remote), Triggering Sync")
             syncManager.syncInvoice(invoiceId) // Pull it down
             return true
        }
        return false
    }

    override suspend fun attachEInvoice(invoiceId: String, eInvoiceDetails: EInvoiceDetails): Result<Unit> {
        android.util.Log.d(TAG, "[InvoiceRepo] attachEInvoice")
        val invoice = invoiceDao.getInvoice(invoiceId) ?: return Result.Error(Exception("Invoice not found"))
        val updated = invoice.copy(
            eInvoiceDetails = eInvoiceDetails,
            updatedAt = System.currentTimeMillis()
        )
        invoiceDao.insert(updated) // Overwrite
        syncManager.pushInvoice(updated)
        return Result.Success(Unit)
    }

    override suspend fun attachEWayBill(invoiceId: String, eWayBillDetails: EWayBillDetails): Result<Unit> {
        android.util.Log.d(TAG, "[InvoiceRepo] attachEWayBill")
        val invoice = invoiceDao.getInvoice(invoiceId) ?: return Result.Error(Exception("Invoice not found"))
        val updated = invoice.copy(
            eWayBillDetails = eWayBillDetails,
            updatedAt = System.currentTimeMillis()
        )
        invoiceDao.insert(updated)
        syncManager.pushInvoice(updated)
        return Result.Success(Unit)
    }

    override suspend fun invoiceExists(
        firmGstin: String,
        invoiceNumber: String,
        financialYear: String,
        excludeInvoiceId: String?
    ): Boolean {
        val localExists = if (excludeInvoiceId != null) {
            invoiceDao.exists(firmGstin, invoiceNumber, financialYear, excludeInvoiceId)
        } else {
             invoiceDao.exists(firmGstin, invoiceNumber, financialYear)
        }
        
        if (localExists) {
            android.util.Log.d(TAG, "[InvoiceRepo] invoiceExists: True (Local)")
            return true
        }
        
        android.util.Log.d(TAG, "[InvoiceRepo] invoiceExists: False (Local), Checking Remote")
        val remoteExists = firestoreDataSource.invoiceExists(firmGstin, invoiceNumber, financialYear, excludeInvoiceId)
        if (remoteExists) {
            android.util.Log.d(TAG, "[InvoiceRepo] invoiceExists: True (Remote)")
            return true
        }
        return false
    }

    override suspend fun getLastInvoiceSequence(firmGstin: String, financialYear: String): Int {
        android.util.Log.d(TAG, "[InvoiceRepo] getLastInvoiceSequence: Checking Local + Remote")
        val localMax = invoiceDao.getMaxSequence(firmGstin, financialYear) ?: 0
        val remoteMax = firestoreDataSource.getLastInvoiceSequence(firmGstin, financialYear)
        
        android.util.Log.d(TAG, "[InvoiceRepo] Max Sequence: Local=$localMax, Remote=$remoteMax")
        return kotlin.math.max(localMax, remoteMax)
    }

    override suspend fun getLastInvoiceDateEpoch(firmGstin: String, financialYear: String): Long? {
        return invoiceDao.getLastInvoiceDateEpoch(firmGstin, financialYear)
    }

    override suspend fun updateInvoiceStatus(
        invoiceId: String,
        status: InvoiceStatus,
        error: String?
    ): Result<Unit> {
        android.util.Log.d(TAG, "[InvoiceRepo] updateStatus: $status")
        invoiceDao.updateStatus(invoiceId, status, error, System.currentTimeMillis())
        
        if (status == InvoiceStatus.COMPLETED || status == InvoiceStatus.CANCELLED) {
             val invoice = invoiceDao.getInvoice(invoiceId)
             if (invoice != null) {
                 android.util.Log.d(TAG, "[InvoiceRepo] Status Finalized, Pushing to Remote")
                 syncManager.pushInvoice(invoice)
             }
        }
        return Result.Success(Unit)
    }

    override suspend fun archiveInvoice(invoice: Invoice): Result<Unit> {
        android.util.Log.d(TAG, "[InvoiceRepo] archiveInvoice")
        invoiceDao.delete(invoice.invoiceId)
        return firestoreDataSource.archiveInvoice(invoice)
    }

    override suspend fun deleteInvoice(invoiceId: String): Result<Unit> {
        android.util.Log.d(TAG, "[InvoiceRepo] deleteInvoice")
        invoiceDao.delete(invoiceId)
        syncManager.deleteInvoice(invoiceId)
        return Result.Success(Unit)
    }

    override suspend fun isLatestInvoice(firmGstin: String, financialYear: String, sequence: Int): Boolean {
        android.util.Log.d(TAG, "[InvoiceRepo] isLatestInvoice: Strict Online Check")
        return firestoreDataSource.isLatestInvoice(firmGstin, financialYear, sequence)
    }

    override suspend fun getArchivedInvoices(): Result<List<Invoice>> {
        android.util.Log.d(TAG, "[InvoiceRepo] getArchivedInvoices: Online Fetch")
        return firestoreDataSource.getArchivedInvoices()
    }

    override fun observeInvoice(invoiceId: String): Flow<Invoice?> {
        android.util.Log.d(TAG, "[InvoiceRepo] observeInvoice: Flowing from Local DB")
        return invoiceDao.observeInvoice(invoiceId)
    }
}
