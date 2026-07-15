package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlinx.coroutines.tasks.await

class FirestoreFirmDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val collection = firestore.collection("firms")

    suspend fun addFirm(firm: Firm): Result<Unit> =
        try {
            collection.document(firm.firmId).set(firm).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun updateFirm(firm: Firm) = addFirm(firm)

    suspend fun deleteFirm(firmId: String): Result<Unit> =
        try {
            collection.document(firmId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getFirm(firmId: String): Result<Firm> =
        try {
            val doc = collection.document(firmId).get().await()
            if (doc.exists()) {
                 val firm = doc.toObject(Firm::class.java)
                 if (firm != null) Result.Success(firm) else Result.Error(Exception("Failed to parse Firm document"))
            } else {
                 Result.Error(Exception("Firm document does not exist"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getAllFirms(): Result<List<Firm>> =
        try {
            val list = collection.get().await()
                .toObjects(Firm::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun firmExistsByGstin(
        gstin: String,
        excludeFirmId: String?
    ): Boolean {

        val snapshot = firestore.collection("firms")
            .whereEqualTo("gstin", gstin)
            .get()
            .await()

        return snapshot.documents.any { doc ->
            excludeFirmId == null || doc.id != excludeFirmId
        }
    }

    suspend fun firmExistsByNickName(
        nickName: String,
        excludeFirmId: String?
    ): Boolean {
        // Validation: Nicknames are case-insensitive
        val snapshot = firestore.collection("firms")
            .whereEqualTo("nickName", nickName)
            .get()
            .await()

        return snapshot.documents.any { doc ->
            excludeFirmId == null || doc.id != excludeFirmId
        }
    }
}
