package com.dollyplastic.invoiceapp.ui.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Summarize
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus

@Composable
fun SelectionContextBar(
    count: Int,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onMerge: () -> Unit,
    onSelectAll: () -> Unit,
    selectedIds: Set<String>,
    invoices: List<Invoice>,
    mergeOptions: @Composable () -> Unit = {}
) {
    val selectedInvoices = invoices.filter { selectedIds.contains(it.invoiceId) }
    val isAnyProcessing = selectedInvoices.any { it.status != InvoiceStatus.COMPLETED && it.status != InvoiceStatus.CANCELLED }
    
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(16.dp), // Slightly less rounded for a "Bar" feel, but still pill-like
        shadowElevation = 6.dp,
        tonalElevation = 6.dp,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .height(64.dp) // Height to accommodate buttons
            .wrapContentWidth() 
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Close (Red Color per request)
            IconButton(onClick = onClear) {
                Icon(
                    Icons.Default.Close, 
                    "Close", 
                    tint = MaterialTheme.colorScheme.error 
                )
            }
            
            VerticalDivider(modifier = Modifier.height(24.dp).padding(end = 4.dp))
            
            // Count
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(end = 4.dp)
            )
            
            // Actions (Text + Icon)
            
            // Select All
            TextButton(
                onClick = onSelectAll,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                Icon(Icons.Default.SelectAll, null, Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("All")
            }

            // Delete (Single Only)
            if (count == 1) {
                 TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(Icons.Outlined.Delete, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Delete") // Explicit Name
                }
            }
            
            // Share / Merge
            val isSingle = count == 1
            val label = if (isSingle) "" else "Merge"
            val icon = if (isSingle) Icons.Default.Share else Icons.Default.Summarize
            
            Box {
                FilledTonalButton(
                    onClick = if (isSingle) onShare else onMerge, 
                    enabled = !isAnyProcessing,
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    modifier = Modifier.height(36.dp) // Compact height
                ) {
                    Icon(icon, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(label)
                }
                if (!isSingle) {
                    mergeOptions()
                }
            }
        }
    }
}
