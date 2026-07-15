package com.dollyplastic.invoiceapp.ui.screens.home.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.domain.Utils.FormatUtils
import com.dollyplastic.invoiceapp.domain.Utils.FormatUtils.toIndianCurrency
import com.dollyplastic.invoiceapp.ui.screens.home.HomeViewModel

@Composable
fun FirmDashboardCard(
    firms: List<Firm>,
    selectedFirmGstin: String?,
    onFirmSelected: (String?) -> Unit,
    totalSales: Double,
    stockItems: List<HomeViewModel.StockItem>,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedFirm = firms.find { it.gstin == selectedFirmGstin }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f) 
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                // --- Top Row: Firm Selector and Period/Total ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Firm Selector with Premium Look
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { expanded = true }
                            .padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedFirm?.nickName?.ifBlank { selectedFirm.tradeName } ?: "All Firms",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false) // Allow shrinking but don't force expansion if short
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Firm",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(28.dp)
                        )
                        
                        DropdownMenu(
                            expanded = expanded, 
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { Text("All Firms") },
                                onClick = { onFirmSelected(null); expanded = false }
                            )
                            firms.forEach { firm ->
                                DropdownMenuItem(
                                    text = { Text(firm.nickName.ifBlank { firm.tradeName }) },
                                    onClick = { onFirmSelected(firm.gstin); expanded = false }
                                )
                            }
                        }
                    }

                    // Total Sales Pill
                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.TrendingUp, 
                                "Sales", 
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = totalSales.toIndianCurrency(),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Current Stock",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // --- Stock Carousel ---
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(stockItems) { item ->
                        StockItemCard(item)
                    }
                }
            }
        }
    }
}

@Composable
fun StockItemCard(item: HomeViewModel.StockItem) {
    Surface(
        color = Color.White.copy(alpha = 0.95f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(140.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "HSN: ${item.hsn}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "${item.quantity.toInt()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = item.unit,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(bottom = 2.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}
