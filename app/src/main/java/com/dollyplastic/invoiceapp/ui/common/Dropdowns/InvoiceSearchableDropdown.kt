package com.dollyplastic.invoiceapp.ui.common.Dropdowns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppColors.Icon
import androidx.compose.foundation.lazy.items
import com.dollyplastic.invoiceapp.ui.common.Utils.onFocusLost


@Composable
fun <T> InvoiceSearchableDropdown(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T?) -> Unit,
    itemLabel: (T) -> String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isError: Boolean = false,
    errorMessage: String? = null,
    isRequired: Boolean = false
) {

    var expanded by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf(selectedItem?.let { itemLabel(it) } ?: "") }
    var textFieldSize by remember { mutableStateOf(IntSize.Zero) }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Sync when external selection changes
    LaunchedEffect(selectedItem) {
        val newText = selectedItem?.let { itemLabel(it) } ?: ""
        if (newText != text) {
            text = newText
        }
    }

    val filteredItems = remember(text, items) {
        if (text.isBlank()) items
        else items.filter { itemLabel(it).contains(text, ignoreCase = true) }
    }

    Column(modifier.fillMaxWidth()) {

        Box {

            OutlinedTextField(
                value = text,
                onValueChange = {
                    text = it
                    expanded = true
                },
                singleLine = true,
                isError = isError,
                placeholder = { Text(placeholder) },
                label = {
                    Text(
                        label

                    )
                },
                shape = RoundedCornerShape(16.dp),
                textStyle = MaterialTheme.typography.bodyLarge,
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded; focusRequester.requestFocus() }) {
                        Icon(
                            imageVector = if (expanded)
                                Icons.Default.KeyboardArrowUp
                            else
                                Icons.Default.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusLost {
                        val exactMatch = items.find {
                            itemLabel(it).equals(text.trim(), ignoreCase = true)
                        }

                        if (exactMatch != null) {
                            onItemSelected(exactMatch)
                            text = itemLabel(exactMatch)
                        } else {
                            // ❗ Strict Mode
                            text = ""
                            onItemSelected(null)
                        }
                        expanded = false
                    }
                    .onGloballyPositioned {
                        textFieldSize = it.size
                    },
                colors = OutlinedTextFieldDefaults.colors(

                    focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else AppColors.PrimaryBlue,
                    unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else AppColors.Border,
                    focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else AppColors.PrimaryBlue,
                    unfocusedLabelColor = if (isError) MaterialTheme.colorScheme.error else AppColors.MutedForeground,
                    cursorColor = AppColors.PrimaryBlue
                ),
            )

            // 👇 OUR OWN DROPDOWN
            if (expanded) {
                Popup(
                    alignment = Alignment.TopStart,
                    offset = IntOffset(0, textFieldSize.height),
                    onDismissRequest = { expanded = false }
                ) {
                    Surface(
                        color = Color.White, // 👈 force white
                        shadowElevation = 8.dp,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .width(with(LocalDensity.current) {
                                textFieldSize.width.toDp()
                            })
                            .heightIn(max = 250.dp)
                    ) {
                        LazyColumn {

                            if (filteredItems.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp)
                                    ) {
                                        Text("No results found")
                                    }
                                }
                            }

                            items(filteredItems) { item ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            text = itemLabel(item)
                                            onItemSelected(item)
                                            expanded = false
                                            focusManager.clearFocus()
                                        }
                                        .padding(12.dp)
                                ) {
                                    Text(itemLabel(item))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp, top = 6.dp)
            )
        }
    }
}




