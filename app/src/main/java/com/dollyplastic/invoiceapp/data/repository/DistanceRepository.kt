package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.utils.Result

interface DistanceRepository {
    suspend fun setPincodeDistance(pincode1: String, pincode2: String, distance: Int): Result<Unit>
    suspend fun getPincodeDistance(pincode1: String, pincode2: String): Int?
}
