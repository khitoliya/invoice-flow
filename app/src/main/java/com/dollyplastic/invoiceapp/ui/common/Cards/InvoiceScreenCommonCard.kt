package com.dollyplastic.invoiceapp.ui.common.Cards

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import androidx.compose.animation.animateContentSize


import com.dollyplastic.invoiceapp.ui.common.softLayerShadow

@Composable
fun InvoiceScreenCommonCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    iconBgColor: Color,
    modifier: Modifier = Modifier,
    cardContainerColor: Color = Color.White,
    elevation: androidx.compose.ui.unit.Dp = 0.dp, // Unused for native elevation now
    isExpandable: Boolean = false,
    expanded: Boolean = true,
    onExpandChange: ((Boolean) -> Unit)? = null,
    subtitle: String? = null,
    useSoftShadow: Boolean = true,
    content: @Composable () -> Unit
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (useSoftShadow) {
                    Modifier.softLayerShadow(
                        color = Color.Black.copy(alpha = 0.1f),
                        cornersRadius = 24.dp,
                        shadowBlurRadius = 12.dp,
                        offsetY = 4.dp
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardContainerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize()
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = if (isExpandable) {
                    Modifier
                        .fillMaxWidth()
                        .clickable { onExpandChange?.invoke(!expanded) }
                } else Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Icon Box
                    Surface(
                        shape = CircleShape,
                        color = iconBgColor,
                        modifier = Modifier.size(40.dp)
                    ) {
                        CommonsIcon(
                            icon = icon,
                            tint = iconColor,
                            modifier = Modifier
                                .padding(10.dp)
                                .size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Title & Subtitle
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.Black
                            )
                        )
                        if (!expanded && !subtitle.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = AppColors.TextSecondary, fontWeight = FontWeight.Normal
                                )
                            )
                        }
                    }
                }

                if (isExpandable) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        tint = AppColors.PrimaryBlue
                    )
                }
            }

            if (!isExpandable || expanded) {
                Spacer(modifier = Modifier.padding(top = 15.dp))
                // Main Content
                content()
                Spacer(modifier = Modifier.padding(top = 15.dp))
            }
        }
    }
}

@Composable
private fun CommonsIcon(
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}
