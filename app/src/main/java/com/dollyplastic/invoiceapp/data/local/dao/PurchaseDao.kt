package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dollyplastic.invoiceapp.data.models.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY importedAt DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE purchaseId = :id")
    suspend fun getPurchaseById(id: String): Purchase?
    
    @Query("SELECT * FROM purchases WHERE firmId = :firmId ORDER BY importedAt DESC")
    fun getPurchasesByFirm(firmId: String): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase)

    @Update
    suspend fun updatePurchase(purchase: Purchase)

    @Delete
    suspend fun deletePurchase(purchase: Purchase)
    
    @Query("DELETE FROM purchases WHERE purchaseId = :id")
    suspend fun deletePurchaseById(id: String)
}
