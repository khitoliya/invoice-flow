package com.dollyplastic.invoiceapp.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.ui.common.softLayerShadow
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InvoiceRowItem(
    invoice: Invoice,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    showFirmName: Boolean = false,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit, 
    onShare: () -> Unit   
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // SurfaceContainerHighest-ish
        label = "bgColor"
    )
    
    val borderColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary 
        else Color.Transparent, 
        label = "borderColor"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        border = if (isSelected) BorderStroke(1.dp, borderColor) else null,
        tonalElevation = if (isSelected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            // Selection Indicator (Visible in Selection Mode or when Selected)
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.2f))
                        .border(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                
                // Optional Firm Name (Top)
                if (showFirmName) {
                    Text(
                        text = invoice.firm.tradeName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                // Row 1: Invoice Number + Date
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = invoice.invoiceNumber, // Removed Hash
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "• ${invoice.invoiceDate}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Row 2: Party Name
                Text(
                    text = invoice.billToParty?.nickName?.ifBlank { invoice.billToParty.tradeName } 
                          ?: invoice.billToParty?.tradeName 
                          ?: "Cash Sale",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Spacer(modifier = Modifier.height(8.dp))
                
                // Row 3: Items List
                if (invoice.items.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        invoice.items.forEach { invoiceItem ->
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                  Text(
                                      text = "• ${invoiceItem.item.name}",
                                      style = MaterialTheme.typography.bodySmall,
                                      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                      maxLines = 1,
                                      overflow = TextOverflow.Ellipsis,
                                      modifier = Modifier.weight(1f, fill = false)
                                  )
                                  Spacer(Modifier.width(4.dp))
                                  Text(
                                      text = "(HSN: ${invoiceItem.item.hsnCode})",
                                      style = MaterialTheme.typography.labelSmall,
                                      color = MaterialTheme.colorScheme.onSurfaceVariant
                                  )
                             }
                             Row {
                                 Text(
                                      text = "  ${com.dollyplastic.invoiceapp.domain.Utils.FormatUtils.formatQuantity(invoiceItem.quantity)} ${invoiceItem.item.unit} x ₹${com.dollyplastic.invoiceapp.domain.Utils.FormatUtils.formatCurrency(invoiceItem.rate)}",
                                      style = MaterialTheme.typography.bodySmall, // Smaller font for details
                                      color = MaterialTheme.colorScheme.onSurfaceVariant
                                 )
                             }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Row 4: Amount + Status
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(invoice.status)
                    Spacer(Modifier.weight(1f))
                    Text(
                        text = com.dollyplastic.invoiceapp.domain.Utils.FormatUtils.run { invoice.totalInvoiceValue.toIndianCurrency() },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Optional Arrow Indicator
            if (!isSelectionMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun StatusChip(status: InvoiceStatus) {
    val (color, text) = when (status) {
        InvoiceStatus.COMPLETED -> Color(0xFF4CAF50) to "Completed" // Changed from "Paid"
        InvoiceStatus.DRAFT -> Color.Gray to "Draft"
        InvoiceStatus.JSON_GENERATION_FAILED, InvoiceStatus.UPLOAD_FAILED, InvoiceStatus.PARSING_FAILED, InvoiceStatus.VALIDATION_FAILED -> MaterialTheme.colorScheme.error to "Error"
        InvoiceStatus.CANCELLED -> MaterialTheme.colorScheme.error to "Cancelled"
        else -> Color(0xFF2196F3) to "Processing"
    }
    
    // Soft pastel style
    Surface(
        color = color.copy(alpha = 0.1f), 
        shape = RoundedCornerShape(50), 
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(6.dp).background(color, CircleShape))
            Spacer(Modifier.width(6.dp))
            Text(
                text = text, 
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), 
                color = color
            )
        }
    }
}

@Composable
fun MiniSummaryRow(
    thisMonthAmount: Double,
    outstandingAmount: Double
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryCard(
            title = "This Month",
            amount = thisMonthAmount,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        SummaryCard(
            title = "Outstanding",
            amount = outstandingAmount,
            color = Color(0xFFE91E63), // Pink/Red for outstanding
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), // SurfaceContainerHighest
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = com.dollyplastic.invoiceapp.domain.Utils.FormatUtils.run { amount.toIndianCurrency() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun FirmPillTab(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Matches Framer Motion { type: "spring", bounce: 0.2, duration: 0.6 }
    val animSpec = spring<Color>(dampingRatio = 0.8f, stiffness = 100f)

    val textColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        animationSpec = animSpec,
        label = "textColor"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.surface,
        animationSpec = animSpec,
        label = "bgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
        animationSpec = animSpec,
        label = "borderColor"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scaleAnim by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 100f),
        label = "scaleAnim"
    )

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(50),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.padding(vertical = 4.dp).scale(scaleAnim)
    ) {
        Text(
            text = name,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = textColor
        )
    }
}

@Composable
fun AnimatedFirmSelectionRow(
    firms: List<Firm>,
    selectedFirmGstin: String?,
    onFirmSelected: (String?) -> Unit
) {
    var tabWidths by remember { mutableStateOf(mapOf<String, Dp>()) }
    var tabOffsets by remember { mutableStateOf(mapOf<String, Dp>()) }
    var itemHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    val selectedId = selectedFirmGstin ?: "ALL"
    val targetOffset = tabOffsets[selectedId] ?: 0.dp
    val targetWidth = tabWidths[selectedId] ?: 0.dp

    // Matches Framer Motion { type: "spring", bounce: 0.2, duration: 0.6 }
    val dpAnimSpec = spring<Dp>(dampingRatio = 0.8f, stiffness = 100f)

    val offsetAnim by animateDpAsState(
        targetValue = targetOffset,
        animationSpec = dpAnimSpec,
        label = "offsetAnim"
    )
    val widthAnim by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = dpAnimSpec,
        label = "widthAnim"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        val pillHeight = itemHeight - 8.dp
        val pillOffsetY = 4.dp

        // The background animated pill
        if (targetWidth > 0.dp && pillHeight > 0.dp) {
            Box(
                modifier = Modifier
                    .absoluteOffset(x = offsetAnim, y = pillOffsetY)
                    .width(widthAnim)
                    .height(pillHeight)
                    .softLayerShadow(
                        color = AppColors.PrimaryBlue.copy(alpha = 0.3f), // reduced alpha closer to web 30%
                        cornersRadius = 50.dp,
                        shadowBlurRadius = 8.dp, // reduced spread
                        offsetX = 0.dp,
                        offsetY = 3.dp  // subtle vertical offset
                    )
                    .background(AppColors.PrimaryBlue, RoundedCornerShape(50.dp))
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val modifierBlock: @Composable (String) -> Modifier = { id ->
                Modifier.onGloballyPositioned { coords ->
                    val width = with(density) { coords.size.width.toDp() }
                    val height = with(density) { coords.size.height.toDp() }
                    val offset = with(density) { coords.positionInParent().x.toDp() }
                    
                    // Only update if absolute difference > 1.dp to prevent micro-jitter during rendering
                    val currentWidth = tabWidths[id] ?: 0.dp
                    if (kotlin.math.abs(width.value - currentWidth.value) > 1f) {
                        tabWidths = tabWidths + (id to width)
                    }
                    
                    val currentOffset = tabOffsets[id] ?: 0.dp
                    if (kotlin.math.abs(offset.value - currentOffset.value) > 1f) {
                        tabOffsets = tabOffsets + (id to offset)
                    }
                    
                    if (itemHeight != height && height > 0.dp) {
                        itemHeight = height
                    }
                }
            }

            FirmPillTab(
                name = "All Firms",
                isSelected = selectedFirmGstin == null,
                onClick = { onFirmSelected(null) },
                modifier = modifierBlock("ALL")
            )

            firms.forEach { firm ->
                FirmPillTab(
                    name = firm.nickName.ifBlank { firm.tradeName },
                    isSelected = selectedFirmGstin == firm.gstin,
                    onClick = { onFirmSelected(firm.gstin) },
                    modifier = modifierBlock(firm.gstin)
                )
            }
        }
    }
}
