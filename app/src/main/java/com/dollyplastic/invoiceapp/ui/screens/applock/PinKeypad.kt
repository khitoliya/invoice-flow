package com.dollyplastic.invoiceapp.ui.screens.applock



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Backspace
import androidx.compose.material.icons.outlined.Backspace
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.dollyplastic.invoiceapp.ui.common.softLayerShadow
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.theme.AppText

@Composable
fun PinKeypad(
    showPin: Boolean,
    onToggleVisibility: () -> Unit,
    onNumber: (Int) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = listOf(
        listOf(1, 2, 3),
        listOf(4, 5, 6),
        listOf(7, 8, 9),
        listOf(-1, 0, -2)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                row.forEach { value ->
                    val keyModifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .softLayerShadow(
                            color = Color.Black.copy(alpha = 0.05f),
                            cornersRadius = 14.dp,
                            shadowBlurRadius = 8.dp,
                            offsetY = 4.dp
                        )

                    when (value) {
                        -1 -> KeypadIcon(
                            icon = if (showPin) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                            onClick = onToggleVisibility,
                            modifier = keyModifier
                        )

                        -2 -> KeypadIcon(
                            icon = Icons.Outlined.Backspace,
                            onClick = onDelete,
                            modifier = keyModifier
                        )

                        else -> KeypadNumber(
                            number = value,
                            onClick = { onNumber(value) },
                            modifier = keyModifier
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KeypadNumber(
    number: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.Background) // White
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            style = AppText.Heading,
            color = AppColors.Foreground
        )
    }
}

@Composable
fun KeypadIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(AppColors.PrimaryBlue)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White
        )
    }
}

