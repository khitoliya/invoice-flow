package com.dollyplastic.invoiceapp.ui.screens.settings.credentials

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dollyplastic.invoiceapp.data.credentials.Credential
import com.dollyplastic.invoiceapp.ui.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CredentialsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // Load firms on enter
    LaunchedEffect(Unit) {
        viewModel.loadFirms()
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Saved Credentials") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Credential")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
           
            if (state.credentials.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No credentials saved yet.", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                LazyColumn {
                    items(state.credentials) { cred ->
                        CredentialItem(
                            credential = cred,
                            onDelete = { viewModel.deleteCredential(cred.id) }
                        )
                        Divider()
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCredentialDialog(
            initialCredential = null,
            availableFirms = state.firms,
            onDismiss = { showAddDialog = false },
            onSave = { name, user, pass, url, firmId ->
                viewModel.addCredential(name, user, pass, url, firmId = firmId)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun CredentialItem(credential: Credential, onDelete: () -> Unit) {
    var showEditDialog by remember { mutableStateOf(false) }
    val viewModel: SettingsViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()

    ListItem(
        modifier = Modifier.clickable { showEditDialog = true },
        headlineContent = { Text(credential.name) },
        supportingContent = { Text(credential.username) },
        leadingContent = { Icon(Icons.Default.Key, contentDescription = null) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    )

    if (showEditDialog) {
        AddCredentialDialog(
            initialCredential = credential,
            availableFirms = state.firms,
            onDismiss = { showEditDialog = false },
            onSave = { name, user, pass, url, firmId ->
                viewModel.addCredential(name, user, pass, url, id = credential.id, firmId = firmId)
                showEditDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCredentialDialog(
    initialCredential: Credential? = null,
    availableFirms: List<com.dollyplastic.invoiceapp.data.models.Firm> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String?) -> Unit
) {
    var name by remember { mutableStateOf(initialCredential?.name ?: "") }
    var username by remember { mutableStateOf(initialCredential?.username ?: "") }
    var password by remember { mutableStateOf(initialCredential?.password ?: "") }
    var url by remember { mutableStateOf(initialCredential?.url ?: "https://ewaybillgst.gov.in") }
    var selectedFirmId by remember { mutableStateOf(initialCredential?.firmId) }
    var expanded by remember { mutableStateOf(false) } // Dropdown state

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialCredential == null) "Add Credential" else "Edit Credential") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Friendly Name (e.g. E-Way Portal)") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    singleLine = true
                )
                var passwordVisible by remember { mutableStateOf(false) }

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    visualTransformation = if (passwordVisible) androidx.compose.ui.text.input.VisualTransformation.None else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            androidx.compose.material.icons.Icons.Filled.Visibility
                        else
                            androidx.compose.material.icons.Icons.Filled.VisibilityOff

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = if (passwordVisible) "Hide Password" else "Show Password")
                        }
                    }
                )
                // Portal Selection Logic
                // We use a predefined list of portals
                val portals = listOf(
                    "E-Way Bill" to "https://ewaybillgst.gov.in/login.aspx",
                    "E-Invoice" to "https://einvoice1.gst.gov.in/",
                    "Custom" to ""
                )
                
                // Determine initial selection based on URL
                var selectedPortalLabel by remember { 
                    mutableStateOf(
                        portals.find { it.second == url }?.first ?: "Custom"
                    )
                }
                var portalExpanded by remember { mutableStateOf(false) }

                // Portal Dropdown
                ExposedDropdownMenuBox(
                    expanded = portalExpanded,
                    onExpandedChange = { portalExpanded = it }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = selectedPortalLabel,
                        onValueChange = {},
                        label = { Text("Portal Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = portalExpanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = portalExpanded,
                        onDismissRequest = { portalExpanded = false }
                    ) {
                        portals.forEach { (label, defaultUrl) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    selectedPortalLabel = label
                                    if (label != "Custom") {
                                        url = defaultUrl
                                    }
                                    portalExpanded = false
                                }
                            )
                        }
                    }
                }

                // Custom URL Field (Only show if Custom is selected)
                if (selectedPortalLabel == "Custom") {
                    OutlinedTextField(
                        value = url,
                        onValueChange = { url = it },
                        label = { Text("Custom URL") },
                        singleLine = true,
                         modifier = Modifier.fillMaxWidth()
                    )
                }

                // Firm Selection Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    val selectedFirm = availableFirms.find { it.firmId == selectedFirmId }
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor(),
                        readOnly = true,
                        value = selectedFirm?.nickName ?: "Select Firm (Required)",
                        onValueChange = {},
                        label = { Text("Link to Firm *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                        isError = selectedFirmId == null
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        availableFirms.forEach { firm ->
                            DropdownMenuItem(
                                text = { Text(firm.tradeName) },
                                onClick = {
                                    selectedFirmId = firm.firmId
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, username, password, url, selectedFirmId) },
                enabled = name.isNotBlank() && username.isNotBlank() && password.isNotBlank() && selectedFirmId != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
