package com.dollyplastic.invoiceapp.ui.screens.masters.firm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.ui.navigation.Route
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirmListScreen(
    navController: NavController,
    viewModel: FirmViewModel = hiltViewModel()
) {
    val firms by viewModel.firms.collectAsState()
    var firmToDelete by remember { mutableStateOf<Firm?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadFirms()
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firms") },
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate(Route.FirmForm.create())
                        }
                    ) { Text("+ Add") }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            items(firms) { firm ->
                ListItem(
                    headlineContent = { Text(firm.tradeName) },
                    supportingContent = {
                        Column {
                            Text(firm.gstin)
                            Text(
                                "${firm.addressLine1}, ${firm.city}, ${firm.state}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    ,
                    trailingContent = {
                        Row {
                            IconButton(onClick = {
                                navController.navigate(
                                    Route.FirmForm.create(firm.firmId)
                                )
                            }) {
                                Icon(Icons.Default.Edit, null)
                            }
                            IconButton(onClick = {
                                firmToDelete = firm
                            }) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    }
                )
                Divider()
            }
        }
    }

    firmToDelete?.let { firm ->
        AlertDialog(
            onDismissRequest = { firmToDelete = null },
            title = { Text("Delete Firm?") },
            text = { Text("This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteFirm(firm.firmId)
                    firmToDelete = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = {
                    firmToDelete = null
                }) { Text("Cancel") }
            }
        )
    }
}
