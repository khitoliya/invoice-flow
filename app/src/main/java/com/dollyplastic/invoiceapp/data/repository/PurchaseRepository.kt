package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.PurchaseDao
import com.dollyplastic.invoiceapp.data.models.Purchase
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class PurchaseRepository @Inject constructor(
    private val purchaseDao: PurchaseDao
) {
    fun getAllPurchases(): Flow<Result<List<Purchase>>> {
        return purchaseDao.getAllPurchases().map { Result.Success(it) }
    }
    
    fun getPurchasesByFirm(firmId: String): Flow<Result<List<Purchase>>> {
         return purchaseDao.getPurchasesByFirm(firmId).map { Result.Success(it) }
    }

    suspend fun getPurchase(id: String): Result<Purchase> {
        return try {
            val purchase = purchaseDao.getPurchaseById(id)
            if (purchase != null) Result.Success(purchase) else Result.Error(Exception("Purchase not found"))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun savePurchase(purchase: Purchase): Result<Unit> {
        return try {
            purchaseDao.insertPurchase(purchase)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun deletePurchase(id: String): Result<Unit> {
        return try {
            purchaseDao.deletePurchaseById(id)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }
}
