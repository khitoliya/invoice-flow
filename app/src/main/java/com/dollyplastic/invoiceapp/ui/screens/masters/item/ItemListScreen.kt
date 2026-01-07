package com.dollyplastic.invoiceapp.ui.screens.masters.item


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    navController: NavController,
    viewModel: ItemViewModel = hiltViewModel()
) {
    val items by viewModel.items.collectAsState()
    var deleteItem by remember { mutableStateOf<ItemUiModel?>(null) }
    LaunchedEffect(Unit) {
        viewModel.loadItems()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Items") },
                actions = {
                    TextButton(
                        onClick = {
                            navController.navigate(Route.ItemForm.create())
                        }
                    ) {
                        Text("+ Add")
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {

            if (items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    ) {
                        Text(
                            "No items added yet",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            items(items) { item ->
                ListItem(
                    headlineContent = {
                        Text(item.name)
                    },
                    supportingContent = {
                        Column {
                            Text(
                                "HSN: ${item.hsnCode}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "GST: ${item.gstRate}%",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    trailingContent = {
                        Row {
                            IconButton(
                                onClick = {
                                    navController.navigate(
                                        Route.ItemForm.create(item.itemId)
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }

                            IconButton(
                                onClick = {
                                    deleteItem = ItemUiModel(
                                        id = item.itemId,
                                        name = item.name
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                )
                Divider()
            }
        }
    }

    // Delete confirmation dialog
    deleteItem?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteItem = null },
            title = { Text("Delete Item") },
            text = {
                Text("Delete '${item.name}'? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteItem(item.id)
                        deleteItem = null
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteItem = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
