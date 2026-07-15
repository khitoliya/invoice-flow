package com.dollyplastic.invoiceapp.ui.screens.invoice.D_ItemSection

import com.dollyplastic.invoiceapp.ui.common.Cards.InvoiceScreenCommonCard
import com.dollyplastic.invoiceapp.ui.common.Utils.formatIndianCurrency
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.data.models.InvoiceItem
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable

fun InvoiceItemSection(
    items: List<InvoiceItem>,
    itemsMaster: List<com.dollyplastic.invoiceapp.data.models.Item>,
    isFormVisible: Boolean,
    editingItem: InvoiceItem?,
    editingItemIndex: Int? = null, // Added parameter
    onEdit: (Int) -> Unit,
    onRemove: (Int) -> Unit,
    onAddClick: () -> Unit,
    onSave: (InvoiceItem) -> Unit,
    onCancel: () -> Unit
) {
    InvoiceScreenCommonCard(
        title = "Items",
        icon = Icons.Default.ShoppingBag,
        iconColor = AppColors.PrimaryBlue,
        iconBgColor = AppColors.PrimaryBlue.copy(alpha = 0.1f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

            if (items.isEmpty() && !isFormVisible) {
                EmptyState()
            } else {
                items.forEachIndexed { index, line ->
                    
                    // Logic:
                    // If isFormVisible AND this is the item being edited (index == editingItemIndex), 
                    // show FORM here.
                    // Otherwise show Card.
                    
                    val isEditingThisItem = isFormVisible && editingItemIndex == index
                    
                    if (isEditingThisItem) {
                         InvoiceItemForm(
                            itemsMaster = itemsMaster,
                            initialItem = editingItem,
                            onSave = onSave,
                            onCancel = onCancel
                        )
                    } else {
                        ItemCard(
                            index = index + 1,
                            item = line,
                            onEdit = { onEdit(index) },
                            onRemove = { onRemove(index) }
                        )
                    }
                }
            }
            
            // Logic for Add Item Button & New Item Form:
            // If isFormVisible is TRUE but editingItemIndex is NULL, it means we are adding a NEW item.
            // In that case, show the form at the bottom.
            // If isFormVisible is TRUE and editingItemIndex is NOT NULL, we are editing an existing item (handled above).
            // If isFormVisible is FALSE, show "Add Item" button.
            
            if (isFormVisible && editingItemIndex == null) {
                // Showing Form for New Item
                InvoiceItemForm(
                    itemsMaster = itemsMaster,
                    initialItem = null,
                    onSave = onSave,
                    onCancel = onCancel
                )
            } else if (!isFormVisible) {
                // Show Add Button
                OutlinedButton(
                    onClick = onAddClick,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.PrimaryBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = AppColors.PrimaryBlue)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Item", color = AppColors.PrimaryBlue, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val stroke = Stroke(
        width = 4f,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
    )
    val borderColor = AppColors.Border

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(color = AppColors.FieldBackground, shape = RoundedCornerShape(12.dp))

            .drawWithContent {
                drawContent()
                drawRoundRect(
                    color = borderColor,
                    style = stroke,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = CircleShape,
                color = AppColors.PrimaryBlue.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShoppingBag,
                        contentDescription = null,
                        tint = AppColors.PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No items added yet",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = AppColors.TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Tap to add your first item",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = AppColors.TextSecondary
                )
            )
        }
    }
}

@Composable
private fun ItemCard(
    index: Int,
    item: InvoiceItem,
    onEdit: () -> Unit,
    onRemove: () -> Unit
) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, AppColors.Border),
        colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT SIDE: Index, Name, HSN, Pill
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Row: Index + Name
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Index Tag
                    Surface(
                        shape = RoundedCornerShape(percent = 50),
                        color = AppColors.PrimaryBlue.copy(alpha = 0.1f),
                    ) {
                        Text(
                            text = "#$index",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = AppColors.PrimaryBlue
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = item.item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.TextPrimary
                        ),
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // HSN Code
                if (item.item.hsnCode.isNotBlank()) {
                    Text(
                        text = "HSN: ${item.item.hsnCode}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = AppColors.TextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Qty x Rate Pill
                Surface(
                    shape = RoundedCornerShape(6.dp), // Soft rect pill
                    color = AppColors.FieldBackground,
                ) {
                    Text(
                        text = "${item.quantity} × ₹${item.rate}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = AppColors.TextPrimary
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // RIGHT SIDE: Amount + Delete
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.labelSmall.copy(color = AppColors.TextSecondary)
                    )
                    val formattedAmount = formatIndianCurrency(item.taxableValue)
                    Text(
                        text = formattedAmount,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = AppColors.PrimaryBlue,
                            fontSize = com.dollyplastic.invoiceapp.ui.common.Utils.getResponsiveFontSize(formattedAmount.length, MaterialTheme.typography.titleMedium.fontSize)
                        )
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Delete Action
                Surface(
                    shape = CircleShape,
                    color = AppColors.Destructive.copy(alpha = 0.1f),
                    modifier = Modifier
                        .clickable { onRemove() }
                        .size(36.dp) // Fixed click target size
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AppColors.Destructive,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
