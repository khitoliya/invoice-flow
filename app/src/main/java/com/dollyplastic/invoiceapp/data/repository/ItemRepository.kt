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
    suspend fun itemExists(
        name: String,
        hsnCode: String,
        excludeItemId: String? = null
    ): Boolean

}
class ItemRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : ItemRepository {

    private val collection = firestore.collection("items")

    override suspend fun addItem(item: Item):Result<Unit> =
        try {
            collection.document(item.itemId).set(item).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun updateItem(item: Item) = addItem(item)

    override suspend fun deleteItem(itemId: String):Result<Unit> =
        try {
            collection.document(itemId).delete().await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getItem(itemId: String): Result<Item> =
        try {
            val doc = collection.document(itemId).get().await()
            Result.Success(doc.toObject(Item::class.java)!!)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun getAllItems(): Result<List<Item>> =
        try {
            val list = collection.get().await()
                .toObjects(Item::class.java)
            Result.Success(list)
        } catch (e: Exception) {
            Result.Error(e)
        }

    override suspend fun itemExists(
        name: String,
        hsnCode: String,
        excludeItemId: String?
    ): Boolean {
        val normalized = TextNormalizer.normalize(name)

        val snapshot = collection
            .whereEqualTo("normalizedName", normalized)
            .whereEqualTo("hsnCode", hsnCode)
            .get()
            .await()

        return snapshot.documents.any { doc ->
            excludeItemId == null || doc.id != excludeItemId
        }
    }


}