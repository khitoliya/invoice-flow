package com.dollyplastic.invoiceapp.ui.screens.masters.party

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.dollyplastic.invoiceapp.data.models.Party
import com.dollyplastic.invoiceapp.ui.navigation.Route
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartyListScreen(
    navController: NavController,
    viewModel: PartyViewModel = hiltViewModel()
) {
    val parties by viewModel.parties.collectAsState()
    var deleteParty by remember { mutableStateOf<Party?>(null) }
    LaunchedEffect(Unit) {
        viewModel.loadParties()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Parties") },
                actions = {
                    TextButton (onClick = {
                        navController.navigate(Route.PartyForm.route)
                    }) { Text("+ Add") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.padding(padding)
        ) {
            items(parties) { p ->
                ListItem(
                    headlineContent = {
                        Text(
                            if (p.nickName.isNotBlank())
                                "${p.tradeName} (${p.nickName})"
                            else p.tradeName
                        )
                    },
                    supportingContent = {
                        Column {
                            Text(p.gstin)
                            Text(
                                "${p.addressLine1}, ${p.city}, ${p.state}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    },
                    trailingContent = {
                        Row {
                            IconButton (onClick = {
                                navController.navigate(
                                    "party_form?partyId=${p.partyId}"
                                )}
                            ) { Icon(Icons.Default.Edit, null) }

                            IconButton (onClick = {
                                deleteParty = p
                            }) { Icon(Icons.Default.Delete, null) }
                        }
                    }
                )
                Divider()
            }
        }
    }

    deleteParty?.let {
        AlertDialog(
            onDismissRequest = { deleteParty = null },
            title = { Text("Delete Party?") },
            text = { Text("This cannot be undone.") },
            confirmButton = {
                TextButton (onClick = {
                    viewModel.deleteParty(it.partyId)
                    deleteParty = null
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton (onClick = { deleteParty = null } ){ Text("Cancel") }
            }
        )
    }
}
