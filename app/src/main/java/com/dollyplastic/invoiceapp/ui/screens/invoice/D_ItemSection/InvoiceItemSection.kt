package com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.InvoiceItem

@Composable
fun InvoiceItemSection(
    items: List<InvoiceItem>,
    onEdit: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onAddClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

        Text(
            "Items",
            style = MaterialTheme.typography.titleMedium
        )

        items.forEachIndexed { index, line ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEdit(index) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("${line.item.name} (${line.item.hsnCode})")
                        Text("Qty: ${line.quantity} | Rate: ${line.rate}")
                        Text("Amount: ₹${line.taxableValue}")
                    }

                    IconButton(
                        onClick = { onRemove(index) }
                    ) {
                        Icon(Icons.Default.Delete, null)
                    }
                }
            }
        }

        TextButton(onClick = onAddClick) {
            Text("+ Add Item")
        }
    }
}
