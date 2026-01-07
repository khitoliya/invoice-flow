package com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.data.models.Item

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceItemDialog(
    items: List<Item>,
    initial: InvoiceItem? = null,
    onSave: (InvoiceItem) -> Unit,
    onDismiss: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    var selectedItem by remember {
        mutableStateOf(initial?.item)
    }
    var qty by remember {
        mutableStateOf(initial?.quantity?.toString() ?: "")
    }
    var rate by remember {
        mutableStateOf(initial?.rate?.toString() ?: "")
    }

    val qtyValue = qty.toDoubleOrNull()
    val rateValue = rate.toDoubleOrNull()

    val qtyValid = qtyValue != null && qtyValue > 0
    val rateValid = rateValue != null && rateValue > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Item" else "Edit Item") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                /* ---------- ITEM DROPDOWN ---------- */

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedItem?.let {
                            "${it.name} (${it.hsnCode})"
                        } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Item*") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        items.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text("${item.name} (${item.hsnCode})")
                                },
                                onClick = {
                                    selectedItem = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                /* ---------- QUANTITY ---------- */

                OutlinedTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = { Text("Quantity*") },
                    isError = qty.isNotBlank() && !qtyValid,
                    supportingText = {
                        if (qty.isNotBlank() && !qtyValid)
                            Text("Enter valid quantity")
                    }
                )

                /* ---------- RATE ---------- */

                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Rate*") },
                    isError = rate.isNotBlank() && !rateValid,
                    supportingText = {
                        if (rate.isNotBlank() && !rateValid)
                            Text("Enter valid rate")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedItem != null && qtyValid && rateValid,
                onClick = {
                    onSave(
                        InvoiceItem(
                            item = selectedItem!!,
                            quantity = qtyValue!!,
                            rate = rateValue!!,
                            taxableValue = qtyValue * rateValue
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

