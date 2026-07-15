package com.dollyplastic.invoiceapp.ui.components.deletion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.ui.common.deletion.DeletionUiState
import com.dollyplastic.invoiceapp.ui.common.dialogs.PremiumAlertDialog

@Composable
fun InvoiceDeletionDialogs(
    uiState: DeletionUiState,
    onConfirmHardDelete: (Invoice) -> Unit,
    onConfirmArchive: (Invoice, String) -> Unit,
    onWarningAcknowledged: (Invoice) -> Unit,
    onDismiss: () -> Unit,
    onOpenPortal: (Invoice) -> Unit
) {
    val context = LocalContext.current

    when (uiState) {
        is DeletionUiState.ShowHardDeleteConfirm -> {
            PremiumAlertDialog(
                onDismissRequest = onDismiss,
                title = "Delete Invoice?",
                text = "This is the latest invoice. It will be permanently removed and the sequence number reused.",
                icon = Icons.Default.DeleteForever,
                iconColor = MaterialTheme.colorScheme.error,
                confirmButton = {
                    Button(
                        onClick = { onConfirmHardDelete(uiState.invoice) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Delete Forever")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            )
        }

        is DeletionUiState.ShowComplianceWarning -> {
            PremiumAlertDialog(
                onDismissRequest = onDismiss,
                title = "Compliance Warning",
                text = "This invoice is linked to a Government Record (IRN/EWB). You MUST cancel it on the portal first to avoid penalties.\n\nDeleting it locally does NOT cancel the government record.",
                icon = Icons.Default.Warning,
                iconColor = MaterialTheme.colorScheme.error,
                confirmButton = {
                    Button(
                        onClick = {
                             uiState.invoice.eWayBillDetails?.ewayBillNo?.let { ewbNo ->
                                if (ewbNo.isNotBlank()) {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("E-Way Bill No", ewbNo)
                                    clipboard.setPrimaryClip(clip)
                                    android.widget.Toast.makeText(context, "E-Way Bill No Copied!", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                            onOpenPortal(uiState.invoice)
                        }
                    ) {
                        Text("Open Portal")
                    }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = onDismiss) {
                            Text("Abort")
                        }
                        Spacer(Modifier.width(8.dp))
                        TextButton(
                            onClick = { onWarningAcknowledged(uiState.invoice) }
                        ) {
                            Text("Already Cancelled")
                        }
                    }
                }
            )
        }

        is DeletionUiState.ShowCancellationRemark -> {
            var remark by remember { mutableStateOf("") }
             // Note: Standard Alert Dialog structure doesn't easily support TextInput in 'text' using PremiumAlertDialog if we want purely text.
             // We can pass a Composable as 'text' if we overloaded it, but for now I'll use the 'text' slot to hold the input if possible?
             // PremiumAlertDialog takes String text. I should update PremiumAlertDialog to accept Composable content or just use BasicPremiumDialog for this one.
             // Actually, I'll update PremiumAlertDialog to allow content over text.
             // Wait, I'll use BasicPremiumDialog for this custom one to keep it clean.
             
             // ... Wait, let's just make PremiumAlertDialog generic. 
             // I'll stick to BasicPremiumDialog for this complex one since it has input fields.
             
             com.dollyplastic.invoiceapp.ui.common.dialogs.BasicPremiumDialog(onDismissRequest = onDismiss) {
                Column(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Edit, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text("Cancel Invoice", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(16.dp))
                    Text("Did you successfully cancel this invoice on the Government Portal?", style = MaterialTheme.typography.bodyMedium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    Spacer(Modifier.height(16.dp))
                    
                    OutlinedTextField(
                        value = remark,
                        onValueChange = { remark = it },
                        label = { Text("Reason (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    
                    Spacer(Modifier.height(24.dp))
                    
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = onDismiss) { Text("Abort") }
                        Spacer(Modifier.width(8.dp))
                        Button(onClick = { onConfirmArchive(uiState.invoice, remark) }) {
                            Text("Confirm")
                        }
                    }
                }
             }
        }

        else -> { }
    }
}
