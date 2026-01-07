package com.dollyplastic.invoiceapp.ui.components


import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun EntitySelector(
    label: String,
    value: String,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        isError = error != null,
        supportingText = {
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }
        },
        modifier = modifier,
        trailingIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ArrowDropDown, null)
            }
        }
    )
}
