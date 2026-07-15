package com.dollyplastic.invoiceapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dollyplastic.invoiceapp.data.models.ConfigEntity

@Dao
interface ConfigDao {
    @Query("SELECT * FROM configurations WHERE `key` = :key")
    suspend fun getConfig(key: String): ConfigEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(config: ConfigEntity)
}
