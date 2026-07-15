package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.PartyDao
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject

class OfflineFirstPartyRepository @Inject constructor(
    private val partyDao: PartyDao,
    private val firestoreDataSource: FirestorePartyDataSource
) : PartyRepository {

    private val TAG = "OfflineFirstSync"

    override suspend fun addParty(party: Party): Result<Unit> {
        try {
            android.util.Log.d(TAG, "[PartyRepo] Adding Party: ${party.tradeName}")
            partyDao.insert(party)
            firestoreDataSource.addParty(party)
            return Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[PartyRepo] Failed to add party", e)
            return Result.Error(e)
        }
    }

    override suspend fun updateParty(party: Party): Result<Unit> = addParty(party)

    override suspend fun deleteParty(partyId: String): Result<Unit> {
        try {
            android.util.Log.d(TAG, "[PartyRepo] Deleting Party: $partyId")
            partyDao.delete(partyId)
            firestoreDataSource.deleteParty(partyId)
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun getParty(partyId: String): Result<Party> {
        val local = partyDao.getParty(partyId)
        if (local != null) {
             android.util.Log.d(TAG, "[PartyRepo] getParty: Local Hit")
             return Result.Success(local)
        }

        android.util.Log.w(TAG, "[PartyRepo] getParty: Local Miss, Syncing")
        val result = firestoreDataSource.getParty(partyId)
        if (result is Result.Success) {
            partyDao.insert(result.data)
        }
        return result
    }

    override suspend fun getAllParties(): Result<List<Party>> {
        android.util.Log.d(TAG, "[PartyRepo] getAllParties: Local Read")
        return Result.Success(partyDao.getAllParties())
    }

    override fun observeAllParties(): kotlinx.coroutines.flow.Flow<List<Party>> {
        return partyDao.observeAllParties()
    }

    override suspend fun partyExists(gstin: String, excludePartyId: String?): Boolean {
        val localExists = if (excludePartyId != null) {
            partyDao.existsByGstin(gstin, excludePartyId)
        } else {
            partyDao.existsByGstin(gstin)
        }
        if (localExists) {
             android.util.Log.d(TAG, "[PartyRepo] partyExists: True (Local)")
             return true
        }
        
        if (firestoreDataSource.partyExists(gstin, excludePartyId)) return true
        return false
    }
}
