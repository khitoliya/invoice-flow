package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.local.dao.ConfigDao
import com.dollyplastic.invoiceapp.data.models.ConfigEntity
import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.config.GstConfig
import com.dollyplastic.invoiceapp.domain.config.IndianState
import com.dollyplastic.invoiceapp.domain.config.StateConfig
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject

class OfflineFirstConfigRepository @Inject constructor(
    private val configDao: ConfigDao,
    private val firestoreDataSource: FirestoreConfigDataSource
) : ConfigRepository {
    
    private val gson = Gson()

    override suspend fun getGstRates(): Result<List<Double>> {
        val key = "gst_rates"
        // 1. Check Local
        val local = configDao.getConfig(key)
        if (local != null) {
            val type = object : TypeToken<List<Double>>() {}.type
            val rates = gson.fromJson<List<Double>>(local.valueJson, type)
            return Result.Success(rates)
        }
        
        // 2. Sync-on-Miss
        val remote = firestoreDataSource.getGstRates()
        if (remote is Result.Success) {
            val json = gson.toJson(remote.data)
            configDao.insert(ConfigEntity(key, json))
        }
        return remote
    }

    override suspend fun updateGstRates(rates: List<Double>): Result<Unit> {
        // Update Local
        val key = "gst_rates"
        val json = gson.toJson(rates)
        configDao.insert(ConfigEntity(key, json))
        
        // Push Remote
        return firestoreDataSource.updateGstRates(rates)
    }

    override suspend fun getStates(): Result<List<IndianState>> {
        val key = "states"
        val local = configDao.getConfig(key)
        if (local != null) {
            val type = object : TypeToken<List<IndianState>>() {}.type
            val states = gson.fromJson<List<IndianState>>(local.valueJson, type)
            return Result.Success(states)
        }
        
        val remote = firestoreDataSource.getStates()
        if (remote is Result.Success) {
             val json = gson.toJson(remote.data)
            configDao.insert(ConfigEntity(key, json))
        }
        return remote
    }

    override suspend fun updateStates(states: List<IndianState>): Result<Unit> {
        val key = "states"
        val json = gson.toJson(states)
        configDao.insert(ConfigEntity(key, json))
        
        return firestoreDataSource.updateStates(states)
    }
}
