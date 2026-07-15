package com.dollyplastic.invoiceapp.ui.screens.processing.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Invoice

@Composable
fun InvoiceDetailsView(
    invoice: Invoice,
    isComplianceLocked: Boolean,
    onEditClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Invoice: ${invoice.invoiceNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${invoice.firm.tradeName} -> ${invoice.billToParty?.tradeName ?: "Cash"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand"
                    )
                }
            }
            
            // Expanded Content
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Basic Details
                    DetailRow("Date:", invoice.invoiceDate)
                    DetailRow("Amount:", "₹${invoice.totalInvoiceValue}")
                    
                    // Display Transport details unconditionally for now, or check a valid property if needed
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Transport", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    DetailRow("Mode:", invoice.transportDetails.mode.name)
                    DetailRow("Vehicle:", invoice.transportDetails.vehicleNumber ?: "N/A")
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Items", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                    invoice.items.forEach { item ->
                        val total = item.taxableValue + item.gstAmount
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${item.item.name} (x${item.quantity})",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                "₹${String.format("%.2f", total)}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    // Edit Button
                    if (!isComplianceLocked) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onEditClick,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Edit Invoice Details")
                        }
                    } else {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Editing disabled (Compliance Completed)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}
