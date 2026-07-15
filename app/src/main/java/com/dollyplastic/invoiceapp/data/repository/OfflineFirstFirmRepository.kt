package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.FirmDao
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject

class OfflineFirstFirmRepository @Inject constructor(
    private val firmDao: FirmDao,
    private val firestoreDataSource: FirestoreFirmDataSource
) : FirmRepository {

    private val TAG = "OfflineFirstSync"

    override suspend fun addFirm(firm: Firm): Result<Unit> {
        try {
            android.util.Log.d(TAG, "[FirmRepo] Adding Firm: ${firm.tradeName}")
            firmDao.insert(firm)
            firestoreDataSource.addFirm(firm)
            return Result.Success(Unit)
        } catch (e: Exception) {
            android.util.Log.e(TAG, "[FirmRepo] Failed to add firm", e)
            return Result.Error(e)
        }
    }

    override suspend fun updateFirm(firm: Firm): Result<Unit> {
        return addFirm(firm)
    }

    override suspend fun deleteFirm(firmId: String): Result<Unit> {
        try {
            android.util.Log.d(TAG, "[FirmRepo] Deleting Firm: $firmId")
            firmDao.delete(firmId)
            firestoreDataSource.deleteFirm(firmId)
             return Result.Success(Unit)
        } catch (e: Exception) {
             return Result.Error(e)
        }
    }

    override suspend fun getFirm(firmId: String): Result<Firm> {
        val local = firmDao.getFirm(firmId)
        if (local != null) {
             android.util.Log.d(TAG, "[FirmRepo] getFirm: Local Hit")
             return Result.Success(local)
        }
        
        android.util.Log.w(TAG, "[FirmRepo] getFirm: Local Miss, Syncing")
        val result = firestoreDataSource.getFirm(firmId)
        if (result is Result.Success) {
            firmDao.insert(result.data)
        }
        return result
    }

    override suspend fun getAllFirms(): Result<List<Firm>> {
        android.util.Log.d(TAG, "[FirmRepo] getAllFirms: Local Read")
        return Result.Success(firmDao.getAllFirms())
    }

    override fun observeAllFirms(): kotlinx.coroutines.flow.Flow<List<Firm>> {
        return firmDao.observeAllFirms()
    }

    override suspend fun firmExistsByGstin(gstin: String, excludeFirmId: String?): Boolean {
        val localExists = if (excludeFirmId != null) {
            firmDao.existsByGstin(gstin, excludeFirmId)
        } else {
            firmDao.existsByGstin(gstin)
        }
        
        if (localExists) {
             android.util.Log.d(TAG, "[FirmRepo] existsByGstin: True (Local)")
             return true
        }
        
        android.util.Log.d(TAG, "[FirmRepo] existsByGstin: False (Local), Checking Remote")
        if (firestoreDataSource.firmExistsByGstin(gstin, excludeFirmId)) {
            return true
        }
        return false
    }

    override suspend fun firmExistsByNickName(nickName: String, excludeFirmId: String?): Boolean {
        val localExists = if (excludeFirmId != null) {
            firmDao.existsByNickName(nickName, excludeFirmId)
        } else {
             firmDao.existsByNickName(nickName)
        }
        
        if (localExists) {
             android.util.Log.d(TAG, "[FirmRepo] existsByNickName: True (Local)")
             return true
        }
        
         if (firestoreDataSource.firmExistsByNickName(nickName, excludeFirmId)) {
            return true
        }
        return false
    }
}
