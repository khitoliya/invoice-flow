package com.dollyplastic.invoiceapp.data.repository


import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.data.utils.Result
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import com.dollyplastic.invoiceapp.domain.Utils.TextNormalizer

interface ItemRepository {
    suspend fun addItem(item: Item): Result<Unit>
    suspend fun updateItem(item: Item): Result<Unit>
    suspend fun deleteItem(itemId: String): Result<Unit>
    suspend fun getItem(itemId: String): Result<Item>
    suspend fun getAllItems(): Result<List<Item>>
    fun observeAllItems(): kotlinx.coroutines.flow.Flow<List<Item>>
    suspend fun itemExists(
        name: String,
        hsnCode: String,
        excludeItemId: String? = null
    ): Boolean

}