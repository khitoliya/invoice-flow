package com.dollyplastic.invoiceapp.ui.common.Dropdowns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.dollyplastic.invoiceapp.ui.common.TextFields.InvoiceScreenTextField
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun <T> InvoiceSimpleDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    isRequired: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    onClear: (() -> Unit)? = null, // Callback for clearing selection
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var textFieldSize by remember { mutableStateOf(androidx.compose.ui.geometry.Size.Zero) }
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }

    // Toggle dropdown when text field is clicked
    androidx.compose.runtime.LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            if (interaction is androidx.compose.foundation.interaction.PressInteraction.Release) {
                expanded = !expanded
            }
        }
    }

    Box(modifier = modifier) {
        InvoiceScreenTextField(
            value = selectedItem?.let { itemLabel(it) } ?: "",
            onValueChange = {}, // Read-only, handled by selection
            label = label,
            isReadOnly = true, // Prevents keyboard from opening
            isRequired = isRequired,
            containerColor = Color.White,
            isError = isError,
            errorMessage = errorMessage,
            interactionSource = interactionSource,
            trailingIcon = {
                if (selectedItem != null && onClear != null) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        modifier = Modifier.clickable { onClear() }
                    )
                } else {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        // Clickable here too, in case user taps icon directly
                        modifier = Modifier.clickable { expanded = !expanded }
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(with(androidx.compose.ui.platform.LocalDensity.current) { textFieldSize.width.toDp() })
                .heightIn(max = 300.dp)
                .background(Color.White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(text = itemLabel(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Helper extension if we aren't using ExposedDropdownMenuBox. 
// Standard DropdownMenu anchors to the position.
