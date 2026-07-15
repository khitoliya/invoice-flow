package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.utils.Result
import kotlinx.coroutines.flow.Flow

interface InvoiceRepository {
    suspend fun createInvoice(invoice: Invoice): Result<Unit>
    suspend fun updateInvoice(invoice: Invoice): Result<Unit>
    suspend fun getInvoice(invoiceId: String): Result<Invoice>
    suspend fun getAllInvoices(): Result<List<Invoice>>
    
    fun observeFilteredInvoices(firmGstin: String?, query: String?): Flow<List<Invoice>>
    
    suspend fun getInvoicesPaged(
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
    ): Result<List<Invoice>>
    
    suspend fun getFilteredSummary(
        firmGstin: String?, 
        query: String?,
        partyId: String?,
        status: InvoiceStatus?,
        minAmount: Double?,
        maxAmount: Double?,
        hsnCode: String?,
        startEpoch: Long?,
        endEpoch: Long?
    ): Result<Pair<Int, Double>>

    fun observeFinancialYears(): Flow<List<String>>

    suspend fun existsById(invoiceId: String): Boolean

    suspend fun attachEInvoice(
        invoiceId: String,
        eInvoiceDetails: EInvoiceDetails
    ): Result<Unit>

    suspend fun attachEWayBill(
        invoiceId: String,
        eWayBillDetails: EWayBillDetails
    ): Result<Unit>

    suspend fun invoiceExists(
        firmGstin: String,
        invoiceNumber: String,
        financialYear: String,
        excludeInvoiceId: String? = null
    ): Boolean

    suspend fun getLastInvoiceSequence(
        firmGstin: String,
        financialYear: String
    ): Int

    suspend fun getLastInvoiceDateEpoch(
        firmGstin: String,
        financialYear: String
    ): Long?

    suspend fun updateInvoiceStatus(
        invoiceId: String,
        status: InvoiceStatus,
        error: String? = null
    ): Result<Unit>
    suspend fun archiveInvoice(invoice: Invoice): Result<Unit>
    suspend fun deleteInvoice(invoiceId: String): Result<Unit>
    
    suspend fun isLatestInvoice(
        firmGstin: String,
        financialYear: String,
        sequence: Int
    ): Boolean

    suspend fun getArchivedInvoices(): Result<List<Invoice>>
    fun observeInvoice(invoiceId: String): Flow<Invoice?>
}
