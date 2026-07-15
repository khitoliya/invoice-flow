package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreDistanceDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    /* ---------------- NEW PINCODE LOGIC ---------------- */

    private val pincodeCollection = firestore.collection("pincode_distances")

    private fun getPincodeDocId(pin1: String, pin2: String) = "${pin1}_${pin2}"

    suspend fun setPincodeDistance(pin1: String, pin2: String, distance: Int): Result<Unit> =
        try {
            val config = com.dollyplastic.invoiceapp.data.models.PincodeDistance(pin1, pin2, distance)
            pincodeCollection.document(getPincodeDocId(pin1, pin2))
                .set(config)
                .await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    suspend fun getPincodeDistance(pin1: String, pin2: String): Int? =
        try {
            val doc = pincodeCollection.document(getPincodeDocId(pin1, pin2)).get().await()
            doc.toObject(com.dollyplastic.invoiceapp.data.models.PincodeDistance::class.java)?.distanceKm
        } catch (e: Exception) {
            null
        }

    suspend fun getAllDistances(): Result<List<com.dollyplastic.invoiceapp.data.models.PincodeDistance>> =
        try {
            val snapshot = pincodeCollection.get().await()
            val list = snapshot.toObjects(com.dollyplastic.invoiceapp.data.models.PincodeDistance::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }
}
