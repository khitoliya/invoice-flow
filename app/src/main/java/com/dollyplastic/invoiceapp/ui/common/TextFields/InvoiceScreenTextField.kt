package com.dollyplastic.invoiceapp.ui.common.TextFields

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppRadius

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceScreenTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isReadOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isRequired: Boolean = false,
    onBlur: (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    containerColor: Color? = null,
    interactionSource: MutableInteractionSource? = null
) {
    val actualInteractionSource = interactionSource ?: remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        TextFieldContent(
            value = value,
            onValueChange = onValueChange,
            label = label,
            placeholder = placeholder,
            isReadOnly = isReadOnly,
            isError = isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            trailingIcon = trailingIcon,
            isRequired = isRequired,
            onBlur = onBlur,
            containerColor = containerColor,
            interactionSource = actualInteractionSource
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun TextFieldContent(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    isReadOnly: Boolean,
    isError: Boolean,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    trailingIcon: @Composable (() -> Unit)?,
    isRequired: Boolean,
    onBlur: (() -> Unit)?,
    modifier: Modifier = Modifier,
    containerColor: Color? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val actualContainerColor = containerColor ?: if (isReadOnly) AppColors.textFieldGrey else Color.White
    val labelColor = if (isError) MaterialTheme.colorScheme.error else AppColors.TextPrimary

    var hasBeenFocused by remember { androidx.compose.runtime.mutableStateOf(false) }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            val showAsterisk = isRequired || label.contains("*")
            val cleanLabel = label.replace("*", "").trim()
            val labelText = buildAnnotatedString {
                append(cleanLabel)
                if (showAsterisk) {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                        append(" *")
                    }
                }
            }
            Text(text = labelText)
        },
        placeholder = { Text(placeholder, color = AppColors.TextSecondary) },
        isError = isError,
        readOnly = isReadOnly,
        singleLine = true,
        shape = RoundedCornerShape(12.dp), // Slimmer radius
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = actualContainerColor,
            unfocusedContainerColor = actualContainerColor,
            disabledContainerColor = actualContainerColor,
            errorContainerColor = actualContainerColor,
            focusedBorderColor = if (isError) MaterialTheme.colorScheme.error else AppColors.PrimaryBlue,
            unfocusedBorderColor = if (isError) MaterialTheme.colorScheme.error else AppColors.Border,
            errorBorderColor = MaterialTheme.colorScheme.error,
            focusedLabelColor = if (isError) MaterialTheme.colorScheme.error else AppColors.PrimaryBlue,
            unfocusedLabelColor = if (isError) MaterialTheme.colorScheme.error else AppColors.TextSecondary,
            errorLabelColor = MaterialTheme.colorScheme.error,
        ),
        modifier = modifier
            .fillMaxWidth()
            // Removed fixed height to prevent text cutting. Default M3 height (56dp) will be used.
            .onFocusChanged { focusState ->
                 if (focusState.isFocused) {
                     hasBeenFocused = true
                 }
                 if (!focusState.isFocused && hasBeenFocused) {
                     onBlur?.invoke()
                 }
            },
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailingIcon,
        interactionSource = interactionSource
    )
}
