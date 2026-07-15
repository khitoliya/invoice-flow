package com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSearchableDropdown
import com.dollyplastic.invoiceapp.ui.common.Dropdowns.InvoiceSimpleDropdown
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.ui.common.Utils.formatIndianCurrency
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun InvoiceItemForm(
    itemsMaster: List<Item>,
    initialItem: InvoiceItem? = null,
    onSave: (InvoiceItem) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedItem by remember { mutableStateOf(initialItem?.item) }
    var qty by remember { mutableStateOf(initialItem?.quantity?.toString() ?: "") }
    var tare by remember { mutableStateOf(initialItem?.tareWeight?.toString() ?: "") }
    var rate by remember { mutableStateOf(initialItem?.rate?.toString() ?: "") }
    
    // Validation State
    var isSubmitted by remember { mutableStateOf(false) }

    val errors = com.dollyplastic.invoiceapp.domain.Validation.InvoiceItemValidator.validateInput(
        item = selectedItem,
        qtyStr = qty,
        tareStr = tare,
        rateStr = rate
    )

    val isItemValid = !errors.containsKey("item")
    val isQtyValid = !errors.containsKey("qty")
    val isTareValid = !errors.containsKey("tare")
    val isRateValid = !errors.containsKey("rate")

    val qtyValue = qty.toDoubleOrNull()
    val tareValue = tare.toDoubleOrNull()
    val rateValue = rate.toDoubleOrNull()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.PrimaryBlue),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HEADER
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = AppColors.PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (initialItem == null) "New Item" else "Edit Item",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryBlue
                        )
                    )
                }
                
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = AppColors.TextSecondary,
                    modifier = Modifier
                        .clickable { onCancel() }
                        .padding(4.dp)
                )
            }

            // ITEM SELECTION
            InvoiceSimpleDropdown(
                label = "Select Item*",
                items = itemsMaster,
                selectedItem = selectedItem,
                onItemSelected = { 
                    selectedItem = it
                },
                itemLabel = { "${it.name} (${it.hsnCode})" },
                isError = isSubmitted && !isItemValid,
                errorMessage = if (isSubmitted && !isItemValid) errors["item"] else null
            )

            // QTY & RATE ROW
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InvoiceScreenTextField(
                    value = qty,
                    onValueChange = { qty = it },
                    label = "Qty *",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = isSubmitted && !isQtyValid,
                    errorMessage = if (isSubmitted && !isQtyValid) errors["qty"] else null
                )

                InvoiceScreenTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = "Rate *",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = isSubmitted && !isRateValid,
                    errorMessage = if (isSubmitted && !isRateValid) errors["rate"] else null
                )
            }

            // TARE ROW
            InvoiceScreenTextField(
                value = tare,
                onValueChange = { tare = it },
                label = "Tare Weight (Optional)",
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = isSubmitted && !isTareValid,
                errorMessage = if (isSubmitted && !isTareValid) errors["tare"] else null
            )
            
            // AMOUNT ROW
            if (isQtyValid && isRateValid) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = AppColors.PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Amount",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                        )

                        val amount = (qtyValue ?: 0.0) * (rateValue ?: 0.0)
                        Text(
                            text = formatIndianCurrency(amount),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryBlue
                            )
                        )
                    }
                }
            }

            // ACTIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AppColors.Border)
                ) {
                    Text("Cancel", color = AppColors.TextSecondary)
                }

                // Save Button
                Button(
                    onClick = {
                        isSubmitted = true
                        if (isItemValid && isQtyValid && isTareValid && isRateValid) {
                            onSave(
                                InvoiceItem(
                                    item = selectedItem!!,
                                    quantity = qtyValue!!,
                                    tareWeight = if (tare.isBlank()) null else tareValue,
                                    rate = rateValue!!,
                                    taxableValue = qtyValue * rateValue
                                )
                            )
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Item")
                }
            }
        }
    }
}
