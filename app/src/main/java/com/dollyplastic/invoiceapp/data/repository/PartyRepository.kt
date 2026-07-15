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
    fun observeAllParties(): kotlinx.coroutines.flow.Flow<List<Party>>
    suspend fun partyExists(
        gstin: String,
        excludePartyId: String? = null
    ): Boolean


}

