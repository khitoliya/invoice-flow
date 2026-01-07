package com.dollyplastic.invoiceapp.ui.screens.masters.firm

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.ui.components.ConfirmDetailsDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmFormScreen(
    navController: NavController,
    firmId: String?,
    viewModel: FirmViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is FirmUiEvent.ShowErrorDialog ->
                    showErrorDialog = event.message

                FirmUiEvent.ShowConfirmDialog ->
                    showConfirmDialog = true
            }
        }
    }



    LaunchedEffect(firmId) {
        firmId?.let { viewModel.loadFirmForEdit(it) }
    }

    showErrorDialog?.let { message ->
        AlertDialog(
            onDismissRequest = { showErrorDialog = null },
            title = { Text("Error") },
            text = { Text(message) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = null }) {
                    Text("OK")
                }
            }
        )
    }



    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (firmId == null) "Add Firm" else "Edit Firm")
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            // Trade Name
            OutlinedTextField(
                value = state.tradeName,
                onValueChange = {
                    viewModel.onFieldChange("tradeName", it)
                },
                label = { Text("Trade Name*") },
                isError = state.errors.containsKey("tradeName"),
                supportingText = {
                    state.errors["tradeName"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // GSTIN
            OutlinedTextField(
                value = state.gstin,
                onValueChange = {
                    viewModel.onGstinChange(it)
                },
                label = { Text("GSTIN*") },
                isError = state.errors.containsKey("gstin"),
                supportingText = {
                    state.errors["gstin"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            // Nick Name (optional but recommended)
            OutlinedTextField(
                value = state.nickName,
                onValueChange = {
                    viewModel.onFieldChange("nickName", it)
                },
                label = { Text("Nick Name") },
                supportingText = {
                    Text(
                        "Short internal name (optional)",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )


            // Address Line 1
            OutlinedTextField(
                value = state.addressLine1,
                onValueChange = {
                    viewModel.onFieldChange("addressLine1", it)
                },
                label = { Text("Address Line 1*") },
                isError = state.errors.containsKey("addressLine1"),
                supportingText = {
                    state.errors["addressLine1"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // Address Line 2 (optional)
            OutlinedTextField(
                value = state.addressLine2,
                onValueChange = {
                    viewModel.onFieldChange("addressLine2", it)
                },
                label = { Text("Address Line 2") }
            )

            // City
            OutlinedTextField(
                value = state.city,
                onValueChange = {
                    viewModel.onFieldChange("city", it)
                },
                label = { Text("City*") },
                isError = state.errors.containsKey("city"),
                supportingText = {
                    state.errors["city"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // State
            OutlinedTextField(
                value = state.state,
                onValueChange = {

                },
                label = { Text("State*") },
                isError = state.errors.containsKey("state"),
                supportingText = {
                    state.errors["state"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            // State Code
            OutlinedTextField(
                value = state.stateCode,
                onValueChange = {},
                label = { Text("State Code") },
                readOnly = true
            )


            // Pincode
            OutlinedTextField(
                value = state.pincode,
                onValueChange = {
                    viewModel.onFieldChange("pincode", it)
                },
                label = { Text("Pincode*") },
                isError = state.errors.containsKey("pincode"),
                supportingText = {
                    state.errors["pincode"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Text(
                text = "Bank Details",
                style = MaterialTheme.typography.titleMedium
            )

            OutlinedTextField(
                value = state.bankName,
                onValueChange = { viewModel.onFieldChange("bankName", it) },
                label = { Text("Bank Name*") },
                isError = state.errors.containsKey("bankName"),
                supportingText = {
                    state.errors["bankName"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            OutlinedTextField(
                value = state.accountNumber,
                onValueChange = { viewModel.onFieldChange("accountNumber", it) },
                label = { Text("Account Number*") },
                isError = state.errors.containsKey("accountNumber"),
                supportingText = {
                    state.errors["accountNumber"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            OutlinedTextField(
                value = state.ifscCode,
                onValueChange = { viewModel.onFieldChange("ifscCode", it) },
                label = { Text("IFSC Code*") },
                isError = state.errors.containsKey("ifscCode"),
                supportingText = {
                    state.errors["ifscCode"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
            OutlinedTextField(
                value = state.branchName,
                onValueChange = { viewModel.onFieldChange("branchName", it) },
                label = { Text("Branch Name*") },
                isError = state.errors.containsKey("branchName"),
                supportingText = {
                    state.errors["branchName"]?.let {
                        Text(it, color = MaterialTheme.colorScheme.error)
                    }
                }
            )



            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    viewModel.requestSaveConfirmation()
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }
    }
    val confirmDetails = listOf(
        "Trade Name" to state.tradeName,
        "Nick Name" to state.nickName,
        "GSTIN" to state.gstin,
        "State" to state.state,
        "State Code" to state.stateCode,
        "Address Line 1" to state.addressLine1,
        "City" to state.city,
        "Pincode" to state.pincode,
        "Bank Name" to state.bankName,
        "Account No" to state.accountNumber,
        "IFSC" to state.ifscCode,
        "Branch" to state.branchName
    )

    if (showConfirmDialog) {
        ConfirmDetailsDialog(
            title = "Confirm Firm Details",
            details = confirmDetails,
            onConfirm = {
                showConfirmDialog = false
                viewModel.saveFirm {
                    navController.popBackStack()
                }
            },
            onDismiss = { showConfirmDialog = false }
        )
    }


}

