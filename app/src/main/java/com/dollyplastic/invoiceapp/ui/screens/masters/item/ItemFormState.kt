package com.dollyplastic.invoiceapp.ui.screens.masters.item


import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.domain.Utils.TextNormalizer
import java.util.UUID

data class ItemFormState(
    val itemId: String? = null,
    val name: String = "",
    val hsnCode: String = "",
    val unit: String = "",
    val gstRate: Double = 0.0,
    val errors: Map<String, String> = emptyMap()
) {

    fun toItem(): Item = Item(
        itemId = itemId ?: UUID.randomUUID().toString(),
        name = name,
        hsnCode = hsnCode,
        unit = unit,
        gstRate = gstRate,
        normalizedName = TextNormalizer.normalize(name),
    )

    fun update(field: String, value: String): ItemFormState =
        when (field) {
            "name" -> copy(name = value)
            "hsnCode" -> copy(hsnCode = value)
            "unit" -> copy(unit = value)
            else -> this
        }

    fun updateGstRate(rate: Double) =
        copy(gstRate = rate)

    companion object {
        fun fromItem(item: Item) = ItemFormState(
            itemId = item.itemId,
            name = item.name,
            hsnCode = item.hsnCode,
            unit = item.unit,
            gstRate = item.gstRate
        )
    }
}
