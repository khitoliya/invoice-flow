package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.*
import com.dollyplastic.invoiceapp.data.models.Item

@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<Item>

    @Query("SELECT * FROM items")
    fun observeAllItems(): kotlinx.coroutines.flow.Flow<List<Item>>

    @Query("SELECT * FROM items WHERE itemId = :itemId")
    suspend fun getItem(itemId: String): Item?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: Item)

    @Query("DELETE FROM items WHERE itemId = :itemId")
    suspend fun delete(itemId: String)

    // Note: Querying separate fields. 'normalizedName' is not in standard Item unless added.
    // Assuming Item has 'name' and 'hsnCode'. Normalization usually happens in Logic.
    // For now we check exact match on name or normalizedName if present in DB.
    // The previous implementation used TextNormalizer.normalize(name).
    // Ideally we store normalizedName in DB.
    
    @Query("SELECT EXISTS(SELECT 1 FROM items WHERE name = :name AND hsnCode = :hsnCode AND itemId != :excludeId)")
    suspend fun exists(name: String, hsnCode: String, excludeId: String): Boolean
    
    @Query("SELECT EXISTS(SELECT 1 FROM items WHERE name = :name AND hsnCode = :hsnCode)")
    suspend fun exists(name: String, hsnCode: String): Boolean
}
