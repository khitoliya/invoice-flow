package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.utils.Result
import com.dollyplastic.invoiceapp.domain.config.GstConfig
import com.dollyplastic.invoiceapp.domain.config.IndianState
import com.dollyplastic.invoiceapp.domain.config.StateConfig
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreConfigDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    private val collection = firestore.collection("configurations")

    suspend fun getGstRates(): Result<List<Double>> =
        try {
            val doc = collection.document("gst_config").get().await()
            if (doc.exists()) {
                val rates = doc.get("rates") as? List<Double>
                Result.Success(rates ?: GstConfig.ALLOWED_GST_RATES)
            } else {
                // Initialize default
                updateGstRates(GstConfig.ALLOWED_GST_RATES)
                Result.Success(GstConfig.ALLOWED_GST_RATES)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun updateGstRates(rates: List<Double>): Result<Unit> =
        try {
            collection.document("gst_config")
                .set(mapOf("rates" to rates))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getStates(): Result<List<IndianState>> =
        try {
            val doc = collection.document("state_config").get().await()
            if (doc.exists()) {
                // Firestore stores objects as HashMaps, need mapping if simple casting fails. 
                // Using toObject helper is safer if we wrap list in a data class, but for list of custom objects:
                // Let's rely on Firestore's ability to map to objects if structure matches.
                // Or safely manually map.
                val list = doc.toObject(StateListWrapper::class.java)?.states
                Result.Success(list ?: StateConfig.STATES)
            } else {
                updateStates(StateConfig.STATES)
                Result.Success(StateConfig.STATES)
            }
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun updateStates(states: List<IndianState>): Result<Unit> =
        try {
            collection.document("state_config")
                .set(StateListWrapper(states))
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }
}

// Helper for Firestore Serialization
data class StateListWrapper(
    val states: List<IndianState> = emptyList()
)
