package com.dollyplastic.invoiceapp.data.repository

import com.dollyplastic.invoiceapp.data.models.Firm
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import com.dollyplastic.invoiceapp.data.utils.Result
import kotlinx.coroutines.tasks.await

interface FirmRepository {
    suspend fun addFirm(firm: Firm): Result<Unit>
    suspend fun updateFirm(firm: Firm): Result<Unit>
    suspend fun deleteFirm(firmId: String): Result<Unit>
    suspend fun getFirm(firmId: String): Result<Firm>
    suspend fun getAllFirms(): Result<List<Firm>>
    fun observeAllFirms(): kotlinx.coroutines.flow.Flow<List<Firm>>
    suspend fun firmExistsByGstin(
        gstin: String,
        excludeFirmId: String? = null
    ): Boolean

    suspend fun firmExistsByNickName(
        nickName: String,
        excludeFirmId: String? = null
    ): Boolean
}



