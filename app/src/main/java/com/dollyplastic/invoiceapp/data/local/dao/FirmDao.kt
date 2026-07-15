package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.*
import com.dollyplastic.invoiceapp.data.models.Firm

@Dao
interface FirmDao {
    @Query("SELECT * FROM firms")
    suspend fun getAllFirms(): List<Firm>

    @Query("SELECT * FROM firms")
    fun observeAllFirms(): kotlinx.coroutines.flow.Flow<List<Firm>>

    @Query("SELECT * FROM firms WHERE firmId = :firmId")
    suspend fun getFirm(firmId: String): Firm?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(firm: Firm)

    @Query("DELETE FROM firms WHERE firmId = :firmId")
    suspend fun delete(firmId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM firms WHERE gstin = :gstin AND firmId != :excludeId)")
    suspend fun existsByGstin(gstin: String, excludeId: String): Boolean
    
     @Query("SELECT EXISTS(SELECT 1 FROM firms WHERE gstin = :gstin)")
    suspend fun existsByGstin(gstin: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM firms WHERE nickName = :nickName AND firmId != :excludeId)")
    suspend fun existsByNickName(nickName: String, excludeId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM firms WHERE nickName = :nickName)")
    suspend fun existsByNickName(nickName: String): Boolean
}
