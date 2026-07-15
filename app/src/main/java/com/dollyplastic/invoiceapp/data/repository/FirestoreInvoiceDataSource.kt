package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.Compliance.EInvoice.EInvoiceDetails
import com.dollyplastic.invoiceapp.domain.Compliance.EwayBill.EWayBillDetails
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreInvoiceDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val collection = firestore.collection("invoices")

    suspend fun createInvoice(invoice: Invoice): Result<Unit> =
        try {
            collection.document(invoice.invoiceId)
                .set(invoice)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun existsById(invoiceId: String): Boolean =
        try {
            val doc = collection.document(invoiceId).get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }

    suspend fun getInvoice(invoiceId: String): Result<Invoice> =
        try {
            android.util.Log.d("FirestoreDataSource", "Fetching Invoice: $invoiceId")
            val doc = collection.document(invoiceId).get().await()
            if (doc.exists()) {
                android.util.Log.d("FirestoreDataSource", "Invoice exists: $invoiceId")
                val invoice = doc.toObject(Invoice::class.java)
                if (invoice != null) {
                     Result.Success(invoice)
                } else {
                     android.util.Log.e("FirestoreDataSource", "Invoice parsing returned null for: $invoiceId")
                     Result.Error(Exception("Failed to parse Invoice document"))
                }
            } else {
                android.util.Log.w("FirestoreDataSource", "Invoice document does not exist: $invoiceId")
                Result.Error(Exception("Invoice document does not exist"))
            }
        } catch (e: Exception) {
            android.util.Log.e("FirestoreDataSource", "Error fetching invoice: $invoiceId", e)
            Result.Error(e)
        }

    suspend fun getAllInvoices(): Result<List<Invoice>> =
        try {
            val list = collection.get().await()
                .toObjects(Invoice::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getInvoicesUpdatedAfter(timestamp: Long): Result<List<Invoice>> =
        try {
            val list = collection
                .whereGreaterThan("updatedAt", timestamp)
                .get()
                .await()
                .toObjects(Invoice::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun attachEInvoice(
        invoiceId: String,
        eInvoiceDetails: EInvoiceDetails
    ): Result<Unit> =
        try {
            val updates = mapOf(
                "eInvoiceDetails" to eInvoiceDetails,
                "updatedAt" to System.currentTimeMillis()
            )
            collection.document(invoiceId)
                .update(updates)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun attachEWayBill(
        invoiceId: String,
        eWayBillDetails: EWayBillDetails
    ): Result<Unit> =
        try {
            val updates = mapOf(
                "eWayBillDetails" to eWayBillDetails,
                "updatedAt" to System.currentTimeMillis()
            )
            collection.document(invoiceId)
                .update(updates)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun invoiceExists(
        firmGstin: String,
        invoiceNumber: String,
        financialYear: String,
        excludeInvoiceId: String?
    ): Boolean {

        val snapshot = firestore.collection("invoices")
            .whereEqualTo("firm.gstin", firmGstin)
            .whereEqualTo("invoiceNumber", invoiceNumber)
            .whereEqualTo("financialYear", financialYear)
            .get()
            .await()

        if (snapshot.isEmpty) return false

        // If no exclusion ID provided, any match means exists = true
        if (excludeInvoiceId == null) return true

        // Check if any matched document is NOT the one we are excluding
        return snapshot.documents.any { it.id != excludeInvoiceId }
    }

    suspend fun getLastInvoiceSequence(
        firmGstin: String,
        financialYear: String
    ): Int {
        val activeMax = firestore.collection("invoices")
            .whereEqualTo("firm.gstin", firmGstin)
            .whereEqualTo("financialYear", financialYear)
            .orderBy("invoiceSequence", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .await()
            .documents.firstOrNull()
            ?.getLong("invoiceSequence")
            ?.toInt()
            ?: 0

        return activeMax
    }

    suspend fun updateInvoiceStatus(
        invoiceId: String,
        status: com.dollyplastic.invoiceapp.data.models.InvoiceStatus,
        error: String?
    ): Result<Unit> =
        try {
            android.util.Log.d("InvoiceRepository", "Updating status for $invoiceId to $status")
            val updates = mutableMapOf<String, Any>(
                "status" to status,
                "updatedAt" to System.currentTimeMillis()
            )
            if (error != null) updates["processingError"] = error
            
            collection.document(invoiceId)
                .update(updates)
                .await()
            android.util.Log.d("InvoiceRepository", "Status update successful for $invoiceId")
            Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("InvoiceRepository", "Status update failed for $invoiceId", e)
            Result.Error(e)
        }


    suspend fun archiveInvoice(invoice: Invoice): Result<Unit> =
        try {
            // 1. Save to deleted_invoices
            firestore.collection("deleted_invoices")
                .document(invoice.invoiceId)
                .set(invoice.copy(status = com.dollyplastic.invoiceapp.data.models.InvoiceStatus.CANCELLED))
                .await()
                
            // 2. Delete from active invoices
            collection.document(invoice.invoiceId)
                .delete()
                .await()
                
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun deleteInvoice(invoiceId: String): Result<Unit> =
        try {
            collection.document(invoiceId)
                .delete()
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun isLatestInvoice(
        firmGstin: String,
        financialYear: String,
        sequence: Int
    ): Boolean {
        return try {
            val lastSeq = getLastInvoiceSequence(firmGstin, financialYear)
            sequence == lastSeq
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getArchivedInvoices(): Result<List<Invoice>> =
        try {
            val list = firestore.collection("deleted_invoices")
                .orderBy("invoiceDate", Query.Direction.DESCENDING)
                .get()
                .await()
                .toObjects(Invoice::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    fun observeInvoice(invoiceId: String): Flow<Invoice> = callbackFlow {

        val listener = firestore
            .collection("invoices")
            .document(invoiceId)
            .addSnapshotListener { snapshot, error ->

                if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener

                val invoice = snapshot.toObject(Invoice::class.java)
                if (invoice != null) trySend(invoice)
            }

        awaitClose { listener.remove() }
    }

}
