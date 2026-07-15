package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.ItemDao
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject

class OfflineFirstItemRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val firestoreDataSource: FirestoreItemDataSource
) : ItemRepository {

    private val TAG = "OfflineFirstSync"

    override suspend fun addItem(item: Item): Result<Unit> {
          try {
            android.util.Log.d(TAG, "[ItemRepo] Adding Item: ${item.name}")
            itemDao.insert(item)
            firestoreDataSource.addItem(item)
            return Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[ItemRepo] Failed to add item", e)
            return Result.Error(e)
        }
    }

    override suspend fun updateItem(item: Item): Result<Unit> = addItem(item)

    override suspend fun deleteItem(itemId: String): Result<Unit> {
         try {
            android.util.Log.d(TAG, "[ItemRepo] Deleting Item: $itemId")
            itemDao.delete(itemId)
            firestoreDataSource.deleteItem(itemId)
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun getItem(itemId: String): Result<Item> {
        val local = itemDao.getItem(itemId)
        if (local != null) {
             android.util.Log.d(TAG, "[ItemRepo] getItem: Local Hit")
             return Result.Success(local)
        }
        
        android.util.Log.w(TAG, "[ItemRepo] getItem: Local Miss, Syncing")
        val result = firestoreDataSource.getItem(itemId)
         if (result is Result.Success) {
            itemDao.insert(result.data)
        }
        return result
    }

    override suspend fun getAllItems(): Result<List<Item>> {
        android.util.Log.d(TAG, "[ItemRepo] getAllItems: Local Read")
        return Result.Success(itemDao.getAllItems())
    }

    override fun observeAllItems(): kotlinx.coroutines.flow.Flow<List<Item>> {
        return itemDao.observeAllItems()
    }

    override suspend fun itemExists(name: String, hsnCode: String, excludeItemId: String?): Boolean {
        // Note: Logic depends on DAO.
        val localExists = if (excludeItemId != null) {
            itemDao.exists(name, hsnCode, excludeItemId)
        } else {
             itemDao.exists(name, hsnCode)
        }
        if (localExists) {
             android.util.Log.d(TAG, "[ItemRepo] itemExists: True (Local)")
             return true
        }
        
        if (firestoreDataSource.itemExists(name, hsnCode, excludeItemId)) return true
        return false
    }
}
