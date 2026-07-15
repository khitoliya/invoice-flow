package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.*
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {

    @Query("SELECT * FROM invoices ORDER BY invoiceDateEpoch DESC, invoiceSequence DESC")
    suspend fun getAllInvoices(): List<Invoice>

    @Query("""
        SELECT * FROM invoices 
        WHERE (:firmGstin IS NULL OR firm LIKE '%"gstin":"' || :firmGstin || '"%') 
        AND (:query IS NULL OR invoiceNumber LIKE '%' || :query || '%')
        AND (:partyId IS NULL OR billToParty LIKE '%"partyId":"' || :partyId || '"%')
        AND (:status IS NULL OR status = :status)
        AND (:minAmount IS NULL OR totalInvoiceValue >= :minAmount)
        AND (:maxAmount IS NULL OR totalInvoiceValue <= :maxAmount)
        AND (:hsnCode IS NULL OR items LIKE '%' || :hsnCode || '%')
        AND (:startEpoch IS NULL OR invoiceDateEpoch >= :startEpoch)
        AND (:endEpoch IS NULL OR invoiceDateEpoch <= :endEpoch)
        ORDER BY invoiceDateEpoch DESC, invoiceSequence DESC
        LIMIT :limit OFFSET :offset
    """)
    suspend fun getInvoicesPaged(
        firmGstin: String?, 
        query: String?,
        partyId: String?,
        status: String?, /* ENUM passed as Name */
        minAmount: Double?,
        maxAmount: Double?,
        hsnCode: String?,
        startEpoch: Long?,
        endEpoch: Long?,
        limit: Int, 
        offset: Int
    ): List<Invoice>

    @Query("""
        SELECT COUNT(*) as count, SUM(totalInvoiceValue) as totalValue FROM invoices 
        WHERE (:firmGstin IS NULL OR firm LIKE '%"gstin":"' || :firmGstin || '"%') 
        AND (:query IS NULL OR invoiceNumber LIKE '%' || :query || '%')
        AND (:partyId IS NULL OR billToParty LIKE '%"partyId":"' || :partyId || '"%')
        AND (:status IS NULL OR status = :status)
        AND (:minAmount IS NULL OR totalInvoiceValue >= :minAmount)
        AND (:maxAmount IS NULL OR totalInvoiceValue <= :maxAmount)
        AND (:hsnCode IS NULL OR items LIKE '%' || :hsnCode || '%')
        AND (:startEpoch IS NULL OR invoiceDateEpoch >= :startEpoch)
        AND (:endEpoch IS NULL OR invoiceDateEpoch <= :endEpoch)
    """)
    suspend fun getFilteredInvoiceSummary(
        firmGstin: String?, 
        query: String?,
        partyId: String?,
        status: String?, 
        minAmount: Double?,
        maxAmount: Double?,
        hsnCode: String?,
        startEpoch: Long?, 
        endEpoch: Long?
    ): InvoiceSummaryTuple

    @Query("SELECT DISTINCT financialYear FROM invoices ORDER BY financialYear DESC")
    fun observeFinancialYears(): Flow<List<String>>

    @Query("""
        SELECT * FROM invoices 
        WHERE (:firmGstin IS NULL OR firm LIKE '%"gstin":"' || :firmGstin || '"%') 
        AND (:query IS NULL OR invoiceNumber LIKE '%' || :query || '%')
        ORDER BY invoiceDateEpoch DESC, invoiceSequence DESC
    """)
    fun observeFilteredInvoices(firmGstin: String?, query: String?): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE invoiceId = :invoiceId")
    suspend fun getInvoice(invoiceId: String): Invoice?

    @Query("SELECT * FROM invoices WHERE invoiceId = :invoiceId")
    fun observeInvoice(invoiceId: String): Flow<Invoice?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(invoice: Invoice)

    @Update
    suspend fun update(invoice: Invoice)

    @Query("DELETE FROM invoices WHERE invoiceId = :invoiceId")
    suspend fun delete(invoiceId: String)

    @Query("UPDATE invoices SET status = :status, processingError = :error, updatedAt = :updatedAt WHERE invoiceId = :invoiceId")
    suspend fun updateStatus(invoiceId: String, status: InvoiceStatus, error: String?, updatedAt: Long)

    // Existence Check
    @Query("SELECT EXISTS(SELECT 1 FROM invoices WHERE firmGstin = :firmGstin AND invoiceNumber = :invoiceNumber AND financialYear = :financialYear AND invoiceId != :excludeId)")
    suspend fun exists(firmGstin: String, invoiceNumber: String, financialYear: String, excludeId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM invoices WHERE firmGstin = :firmGstin AND invoiceNumber = :invoiceNumber AND financialYear = :financialYear)")
    suspend fun exists(firmGstin: String, invoiceNumber: String, financialYear: String): Boolean

    @Query("SELECT MAX(invoiceSequence) FROM invoices WHERE firmGstin = :firmGstin AND financialYear = :financialYear")
    suspend fun getMaxSequence(firmGstin: String, financialYear: String): Int?

    @Query("SELECT MAX(invoiceDateEpoch) FROM invoices WHERE firmGstin = :firmGstin AND financialYear = :financialYear")
    suspend fun getLastInvoiceDateEpoch(firmGstin: String, financialYear: String): Long?
}

data class InvoiceSummaryTuple(
    @ColumnInfo(name = "count") val count: Int,
    @ColumnInfo(name = "totalValue") val totalValue: Double?
)
