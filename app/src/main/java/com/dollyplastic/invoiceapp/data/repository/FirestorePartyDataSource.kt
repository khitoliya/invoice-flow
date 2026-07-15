package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.models.Party
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject

class FirestorePartyDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val collection = firestore.collection("parties")

    suspend fun addParty(party: Party):Result<Unit> =
        try {
            collection.document(party.partyId).set(party).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun updateParty(party: Party) = addParty(party)

    suspend fun deleteParty(partyId: String):Result<Unit> =
        try {
            collection.document(partyId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getParty(partyId: String): Result<Party> =
        try {
            val doc = collection.document(partyId).get().await()
             if (doc.exists()) {
                 val party = doc.toObject(Party::class.java)
                 if (party != null) Result.Success(party) else Result.Error(Exception("Failed to parse Party document"))
            } else {
                 Result.Error(Exception("Party document does not exist"))
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getAllParties(): Result<List<Party>> =
        try {
            val list = collection.get().await()
                .toObjects(Party::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }
    suspend fun partyExists(
        gstin: String,
        excludePartyId: String?
    ): Boolean {
        val snapshot = collection
            .whereEqualTo("gstin", gstin)
            .get()
            .await()

        return snapshot.documents.any { doc ->
            excludePartyId == null || doc.id != excludePartyId
        }
    }
}
