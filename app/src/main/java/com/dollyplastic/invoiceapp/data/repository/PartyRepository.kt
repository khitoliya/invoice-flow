package com.dollyplastic.invoiceapp.data.repository


import com.dollyplastic.invoiceapp.data.models.Party
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject

interface PartyRepository {
    suspend fun addParty(party: Party): Result<Unit>
    suspend fun updateParty(party: Party): Result<Unit>
    suspend fun deleteParty(partyId: String): Result<Unit>
    suspend fun getParty(partyId: String): Result<Party>
    suspend fun getAllParties(): Result<List<Party>>
    suspend fun partyExists(
        gstin: String,
        excludePartyId: String? = null
    ): Boolean


}
class PartyRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : PartyRepository {

    private val collection = firestore.collection("parties")

    override suspend fun addParty(party: Party):Result<Unit> =
        try {
            collection.document(party.partyId).set(party).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun updateParty(party: Party) = addParty(party)

    override suspend fun deleteParty(partyId: String):Result<Unit> =
        try {
            collection.document(partyId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getParty(partyId: String): Result<Party> =
        try {
            val doc = collection.document(partyId).get().await()
            Result.Success(doc.toObject(Party::class.java)!!)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getAllParties(): Result<List<Party>> =
        try {
            val list = collection.get().await()
                .toObjects(Party::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }
    override suspend fun partyExists(
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
