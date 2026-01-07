package com.dollyplastic.invoiceapp.ui.components


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ConfirmDetailsDialog(
    title: String,
    details: List<Pair<String, String>>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                details.forEach { (label, value) ->
                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = value.ifBlank { "-" },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Divider()
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirm & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Edit")
            }
        }
    )
}
