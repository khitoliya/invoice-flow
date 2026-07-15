package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.PincodeDistanceDao
import com.dollyplastic.invoiceapp.data.utils.Result
import javax.inject.Inject

class OfflineFirstDistanceRepository @Inject constructor(
    private val pincodeDistanceDao: PincodeDistanceDao,
    private val firestoreDataSource: FirestoreDistanceDataSource
) : DistanceRepository {

    /* ---------------- NEW PINCODE LOGIC ---------------- */
    
    // Helper to sort pincodes so (A, B) is same as (B, A)
    private fun sortPincodes(p1: String, p2: String): Pair<String, String> {
        return if (p1 < p2) p1 to p2 else p2 to p1
    }

    override suspend fun setPincodeDistance(pincode1: String, pincode2: String, distance: Int): Result<Unit> {
        if (pincode1 == pincode2) return Result.Success(Unit) // Implicitly 15, no need to save? Or save overwrite? 
        // Plan says: "If pin1 == pin2, do nothing"
        
        val (p1, p2) = sortPincodes(pincode1, pincode2)
        
        try {
            val entity = com.dollyplastic.invoiceapp.data.models.PincodeDistance(p1, p2, distance)
            pincodeDistanceDao.insert(entity) // Local
            firestoreDataSource.setPincodeDistance(p1, p2, distance) // Remote
            return Result.Success(Unit)
        } catch (e: Exception) {
            return Result.Error(e)
        }
    }

    override suspend fun getPincodeDistance(pincode1: String, pincode2: String): Int? {
        if (pincode1 == pincode2) return 100
        
        val (p1, p2) = sortPincodes(pincode1, pincode2)
        
        // 1. Local
        val local = pincodeDistanceDao.getDistance(p1, p2)
        if (local != null) return local.distanceKm
        
        // 2. Remote (Sync-on-Miss)
        val remoteDist = firestoreDataSource.getPincodeDistance(p1, p2)
        if (remoteDist != null) {
            val entity = com.dollyplastic.invoiceapp.data.models.PincodeDistance(p1, p2, remoteDist)
            pincodeDistanceDao.insert(entity)
            return remoteDist
        }
        
        return null
    }
}
