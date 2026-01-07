package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.models.Firm
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.dollyplastic.invoiceapp.data.utils.Result
import kotlinx.coroutines.tasks.await

interface FirmRepository {
    suspend fun addFirm(firm: Firm): Result<Unit>
    suspend fun updateFirm(firm: Firm): Result<Unit>
    suspend fun deleteFirm(firmId: String): Result<Unit>
    suspend fun getFirm(firmId: String): Result<Firm>
    suspend fun getAllFirms(): Result<List<Firm>>
    suspend fun firmExistsByGstin(
        gstin: String,
        excludeFirmId: String? = null
    ): Boolean


}
class FirmRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : FirmRepository {

    private val collection = firestore.collection("firms")

    override suspend fun addFirm(firm: Firm): Result<Unit> =
        try {
            collection.document(firm.firmId).set(firm).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun updateFirm(firm: Firm) = addFirm(firm)

    override suspend fun deleteFirm(firmId: String): Result<Unit> =
        try {
            collection.document(firmId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getFirm(firmId: String): Result<Firm> =
        try {
            val doc = collection.document(firmId).get().await()
            Result.Success(doc.toObject(Firm::class.java)!!)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getAllFirms(): Result<List<Firm>> =
        try {
            val list = collection.get().await()
                .toObjects(Firm::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun firmExistsByGstin(
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


}

