package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.*
import com.dollyplastic.invoiceapp.data.models.Party

@Dao
interface PartyDao {
    @Query("SELECT * FROM parties")
    suspend fun getAllParties(): List<Party>

    @Query("SELECT * FROM parties")
    fun observeAllParties(): kotlinx.coroutines.flow.Flow<List<Party>>

    @Query("SELECT * FROM parties WHERE partyId = :partyId")
    suspend fun getParty(partyId: String): Party?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(party: Party)

    @Query("DELETE FROM parties WHERE partyId = :partyId")
    suspend fun delete(partyId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM parties WHERE gstin = :gstin AND partyId != :excludeId)")
    suspend fun existsByGstin(gstin: String, excludeId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM parties WHERE gstin = :gstin)")
    suspend fun existsByGstin(gstin: String): Boolean
}
