package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dollyplastic.invoiceapp.data.models.PincodeDistance

@Dao
interface PincodeDistanceDao {

    @Query("SELECT * FROM pincode_distances WHERE pincode1 = :pincode1 AND pincode2 = :pincode2")
    suspend fun getDistance(pincode1: String, pincode2: String): PincodeDistance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(distance: PincodeDistance)

    @Query("SELECT * FROM pincode_distances")
    suspend fun getAll(): List<PincodeDistance>
}
