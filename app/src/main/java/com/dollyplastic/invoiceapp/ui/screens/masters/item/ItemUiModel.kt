package com.dollyplastic.invoiceapp.ui.screens.masters.item

import com.dollyplastic.invoiceapp.data.models.Item

data class FirmStock(
    val firmId: String,
    val firmName: String,
    val stockQty: Int
)

data class ItemUiModel(
    val item: Item,
    val totalStock: Int,
    val firmStocks: List<FirmStock>,
    val isExpanded: Boolean = false
)
