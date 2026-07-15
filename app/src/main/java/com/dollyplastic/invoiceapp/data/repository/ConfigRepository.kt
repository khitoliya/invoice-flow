package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.config.IndianState

interface ConfigRepository {
    suspend fun getGstRates(): Result<List<Double>>
    suspend fun updateGstRates(rates: List<Double>): Result<Unit>
    
    suspend fun getStates(): Result<List<IndianState>>
    suspend fun updateStates(states: List<IndianState>): Result<Unit>
}
