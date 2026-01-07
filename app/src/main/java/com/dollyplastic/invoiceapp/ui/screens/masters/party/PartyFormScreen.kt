package com.dollyplastic.invoiceapp.ui.screens.masters.party




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
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartyViewModel.PartyUiEvent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyFormScreen(
    navController: NavController,
    partyId: String?,
    viewModel: PartyViewModel = hiltViewModel()
) {
    val state by viewModel.formState.collectAsState()
    val isFormValid by viewModel.isFormValid.collectAsState()
    var showErrorDialog by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }



    LaunchedEffect(partyId) {
        partyId?.let { viewModel.loadPartyForEdit(it) }
    }
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is PartyUiEvent.ShowErrorDialog -> {
                    showErrorDialog = event.message
                }
                PartyUiEvent.ShowConfirmDialog -> {
                    showConfirmDialog = true
                }
            }
        }
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
                    Text(if (partyId == null) "Add Party" else "Edit Party")

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
                onValueChange = { },
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
        "Party Name" to state.tradeName,
        "Nick Name" to state.nickName,
        "GSTIN" to state.gstin,
        "State" to state.state,
        "State Code" to state.stateCode,
        "Address Line 1" to state.addressLine1,
        "Address Line 2" to (state.addressLine2 ?: ""),
        "City" to state.city,
        "Pincode" to state.pincode
    )


    if (showConfirmDialog) {
        ConfirmDetailsDialog(
            title = "Confirm Party Details",
            details = confirmDetails,
            onConfirm = {
                showConfirmDialog = false
                viewModel.saveParty {
                    navController.popBackStack()
                }
            },
            onDismiss = {
                showConfirmDialog = false
            }
        )
    }

}



