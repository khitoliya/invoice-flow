package com.dollyplastic.invoiceapp.ui.screens.import_workflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Purchase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@Composable
fun VerificationForm(
    initialPurchase: Purchase,
    onSave: (Purchase) -> Unit
) {
    var invoiceNumber by remember { mutableStateOf(initialPurchase.vendorInvoiceNumber) }
    var date by remember { mutableStateOf(initialPurchase.purchaseDate) }
    var vendorName by remember { mutableStateOf(initialPurchase.vendor.tradeName) }
    var totalAmount by remember { mutableStateOf(initialPurchase.totalAmount.toString()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Verify Purchase Bill", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = invoiceNumber,
            onValueChange = { invoiceNumber = it },
            label = { Text("Vendor Invoice No") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = vendorName,
            onValueChange = { vendorName = it },
            label = { Text("Vendor Name") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = totalAmount,
            onValueChange = { totalAmount = it },
            label = { Text("Total Amount") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val updated = initialPurchase.copy(
                    vendorInvoiceNumber = invoiceNumber,
                    purchaseDate = date,
                    vendor = initialPurchase.vendor.copy(tradeName = vendorName),
                    totalAmount = totalAmount.toDoubleOrNull() ?: 0.0
                )
                onSave(updated)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Purchase")
        }
    }
}
