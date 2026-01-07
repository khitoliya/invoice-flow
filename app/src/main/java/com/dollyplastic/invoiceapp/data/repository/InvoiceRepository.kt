package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.models.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillDetails
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

interface InvoiceRepository {
    suspend fun createInvoice(invoice: Invoice): Result<Unit>
    suspend fun getInvoice(invoiceId: String): Result<Invoice>
    suspend fun getAllInvoices(): Result<List<Invoice>>

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
        financialYear: String
    ): Boolean

    suspend fun getLastInvoiceSequence(
        firmGstin: String,
        financialYear: String
    ): Int

    suspend fun updateInvoiceStatus(
        invoiceId: String,
        status: com.dollyplastic.invoiceapp.data.models.InvoiceStatus,
        error: String? = null
    ): Result<Unit>


}

class InvoiceRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : InvoiceRepository {

    private val collection = firestore.collection("invoices")

    override suspend fun createInvoice(invoice: Invoice): Result<Unit> =
        try {
            collection.document(invoice.invoiceId)
                .set(invoice)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getInvoice(invoiceId: String): Result<Invoice> =
        try {
            val doc = collection.document(invoiceId).get().await()
            Result.Success(doc.toObject(Invoice::class.java)!!)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getAllInvoices(): Result<List<Invoice>> =
        try {
            val list = collection.get().await()
                .toObjects(Invoice::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun attachEInvoice(
        invoiceId: String,
        eInvoiceDetails: EInvoiceDetails
    ): Result<Unit> =
        try {
            collection.document(invoiceId)
                .update("eInvoiceDetails", eInvoiceDetails)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun attachEWayBill(
        invoiceId: String,
        eWayBillDetails: EWayBillDetails
    ): Result<Unit> =
        try {
            collection.document(invoiceId)
                .update("eWayBillDetails", eWayBillDetails)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun invoiceExists(
        firmGstin: String,
        invoiceNumber: String,
        financialYear: String
    ): Boolean {

        val snapshot = firestore.collection("invoices")
            .whereEqualTo("firm.gstin", firmGstin)
            .whereEqualTo("invoiceNumber", invoiceNumber)
            .whereEqualTo("financialYear", financialYear)
            .limit(1)
            .get()
            .await()

        return !snapshot.isEmpty
    }

    override suspend fun getLastInvoiceSequence(
        firmGstin: String,
        financialYear: String
    ): Int {
        val snapshot = firestore.collection("invoices")
            .whereEqualTo("firm.gstin", firmGstin)
            .whereEqualTo("financialYear", financialYear)
            .orderBy("invoiceSequence", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()

        return snapshot.documents.firstOrNull()
            ?.getLong("invoiceSequence")
            ?.toInt()
            ?: 0
    }

    override suspend fun updateInvoiceStatus(
        invoiceId: String,
        status: com.dollyplastic.invoiceapp.data.models.InvoiceStatus,
        error: String?
    ): Result<Unit> =
        try {
            val updates = mutableMapOf<String, Any>("status" to status)
            if (error != null) updates["processingError"] = error
            
            collection.document(invoiceId)
                .update(updates)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }


}

