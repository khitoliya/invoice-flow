package com.dollyplastic.invoiceapp.ui.common.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.dollyplastic.invoiceapp.ui.theme.BetmFontFamily
import androidx.compose.ui.window.DialogProperties
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.common.softLayerShadow
import androidx.compose.material.icons.filled.Description

data class DetailSection(
    val title: String,
    val items: List<DetailItem>
)

data class DetailItem(
    val label: String,
    val value: String
)



@Composable
fun ConfirmDetailDialog(
    title: String,
    description: String,
    icon: ImageVector = Icons.Filled.Description,
    sections: List<DetailSection>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isLoading: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // SCROLLABLE CONTENT (Weight 1f)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon Header
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF2F70B7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = title,
                        fontFamily = BetmFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Description
                    Text(
                        text = description,
                        fontFamily = BetmFontFamily,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Sections
                    sections.forEach { section ->
                        DetailSectionView(section)
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // FIXED BOTTOM BUTTONS
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp) 
                ) {
                    Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB))
                        ) {
                            Text(
                                text = cancelText,
                                fontFamily = BetmFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .softLayerShadow(
                                    color = AppColors.PrimaryBlue.copy(alpha = 0.2f),
                                    cornersRadius = 8.dp, // Fully rounded
                                    shadowBlurRadius = 12.dp,
                                    offsetY = 6.dp
                                ),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.PrimaryBlue),
                            enabled = !isLoading
                        ) {
                            Text(
                                text = if (isLoading) "Saving..." else confirmText,
                                fontFamily = BetmFontFamily,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailSectionView(section: DetailSection) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = section.title,
            fontFamily = BetmFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = Color(0xFF111827),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(8.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                section.items.forEachIndexed { index, item ->
                    if (index > 0) {
                        Divider(color = Color(0xFFE5E7EB), thickness = 1.dp)
                    }
                    DetailItemRow(item)
                }
            }
        }
    }
}

@Composable
fun DetailItemRow(item: DetailItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(androidx.compose.foundation.layout.IntrinsicSize.Min) // Force children to have same height
            .background(Color(0xFFF9FAFB)) 
    ) {
        // Label Column
        Box(
            modifier = Modifier
                .width(120.dp)
                .fillMaxHeight() // Stretch to match the row (value) height
                .background(Color(0xFFF3F4F6))
                .padding(12.dp),
            contentAlignment = Alignment.CenterStart // Vertically center the text
        ) {
            Text(
                text = item.label,
                fontFamily = BetmFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = Color(0xFF374151)
            )
        }

        // Value Column
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
                .padding(12.dp)
        ) {
            Text(
                text = item.value,
                fontFamily = BetmFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = Color(0xFF111827)
            )
        }
    }
}
