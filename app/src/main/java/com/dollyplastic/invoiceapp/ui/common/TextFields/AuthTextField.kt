package com.dollyplastic.invoiceapp.ui.common.TextFields

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppRadius
import com.dollyplastic.invoiceapp.ui.theme.AppText


@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector? = null, // Made nullable with default null
    isPassword: Boolean = false,
    showPassword: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    readOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val textStyle = AppText.Input

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 48.dp),
        singleLine = true,
        readOnly = readOnly,
        isError = isError,
        placeholder = {
            Text(
                placeholder,
                style = textStyle,
                color = AppColors.MutedForeground
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    leadingIcon,
                    contentDescription = null,
                    tint = AppColors.Icon
                )
            }
        } else null,
        trailingIcon = {
            if (trailingContent != null) {
                trailingContent()
            } else if (isPassword && onTogglePassword != null) {
                IconButton(onClick = onTogglePassword) {
                    Icon(
                        if (showPassword)
                            Icons.Outlined.VisibilityOff
                        else
                            Icons.Outlined.Visibility,
                        contentDescription = null,
                        tint = AppColors.Icon
                    )
                }
            }
        },
        visualTransformation =
            if (isPassword && !showPassword)
                PasswordVisualTransformation()
            else
                VisualTransformation.None,
        shape = RoundedCornerShape(AppRadius.Lg),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AppColors.PrimaryBlue,
            unfocusedBorderColor = AppColors.Border,
            focusedContainerColor = AppColors.FieldBackground,
            unfocusedContainerColor = AppColors.FieldBackground,
            focusedTextColor = AppColors.TextPrimary,
            unfocusedTextColor = AppColors.TextPrimary,
            cursorColor = AppColors.PrimaryBlue
        ),
        textStyle = textStyle
    )
}



@Composable
fun AuthLabel(text: String) {

    Text(
        text = text,
        style = AppText.Label,
        color = AppColors.Foreground,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp),
        textAlign = TextAlign.Start
    )
}


