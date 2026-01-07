package com.dollyplastic.invoiceapp.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> EntitySelectionDialog(
    title: String,
    items: List<T>,
    displayName: (T) -> String,
    subText: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    var query by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Search") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(
                        items.filter {
                            displayName(it)
                                .contains(query, ignoreCase = true)
                        }
                    ) { item ->
                        ListItem(
                            headlineContent = {
                                Text(displayName(item))
                            },
                            supportingContent = {
                                Text(subText(item))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSelect(item)
                                    onDismiss()
                                }
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
