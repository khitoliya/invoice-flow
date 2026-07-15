package com.dollyplastic.invoiceapp.ui.common.list

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.common.Animations.SmoothExpansion
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun EntityListCard(
    tradeName: String,
    city: String,
    state: String,
    gstin: String,
    nickName: String,
    fullAddress: String,
    bankDetails: String? = null,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 0.dp,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            
            // --- TOP ROW: Avatar + Basic Info + Action ---
            Row(
                verticalAlignment = Alignment.Top
            ) {
                // 1. Avatar
                val initials = getInitials(tradeName)
                val avatarColor = getAvatarColor(tradeName)
                
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = avatarColor
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 2. Main Header Info (Collapsed View)
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Trade Name
                    Text(
                        text = tradeName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111827),
                            fontSize = 16.sp
                        ),
                        maxLines = if (expanded) Int.MAX_VALUE else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // City, State (Always visible)
                    IconTextRow(
                        icon = Icons.Outlined.Place,
                        text = "$city, $state",
                        color = Color(0xFF6B7280)
                    )

                    // GSTIN (Always visible but compact)
                    if (gstin.isNotBlank()) {
                         Spacer(modifier = Modifier.height(2.dp))
                         IconTextRow(
                            icon = Icons.Outlined.Verified,
                            text = gstin,
                            color = Color(0xFF6B7280) // Greenish for verified vibe
                         )
                    }
                }
                
                // 3. Action Menu & Expand Icon
                Column(horizontalAlignment = Alignment.End) {
                     Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Actions",
                                tint = Color(0xFF9CA3AF)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            modifier = Modifier.background(Color.White)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Edit") },
                                onClick = {
                                    showMenu = false
                                    onEdit()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Edit, null, tint = Color(0xFF4B5563))
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete", color = Color(0xFFDC2626)) },
                                onClick = {
                                    showMenu = false
                                    onDelete()
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Delete, null, tint = Color(0xFFDC2626))
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    

                }
            }

            // --- EXPANDED DETAILS SECTION ---
            SmoothExpansion(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFE5E7EB))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Nickname
                    if (nickName.isNotBlank()) {
                        IconTextRow(
                            icon = Icons.Outlined.Person, // Or Label
                            text = "Nickname: $nickName",
                            color = Color(0xFF4B5563),
                            iconTint = AppColors.PrimaryBlue
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    // Full Address
                    if (fullAddress.isNotBlank()) {
                        IconTextRow(
                            icon = Icons.Outlined.Home,
                            text = fullAddress,
                            color = Color(0xFF4B5563),
                            iconTint = AppColors.PrimaryBlue,
                            maxLines = Int.MAX_VALUE
                        )
                    }

                    // Bank Details
                    if (!bankDetails.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        IconTextRow(
                            icon = Icons.Filled.AccountBalance,
                            text = bankDetails,
                            color = Color(0xFF4B5563),
                            iconTint = AppColors.PrimaryBlue,
                            maxLines = Int.MAX_VALUE
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconTextRow(
    icon: ImageVector,
    text: String,
    color: Color,
    iconTint: Color = Color(0xFF9CA3AF), // Gray 400 default
    maxLines: Int = 1
) {
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(16.dp).padding(top = 2.dp) // Align with text cap-height roughly
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(
                color = color
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// --- Helpers ---

private fun getInitials(name: String): String {
    val words = name.trim().split("\\s+".toRegex()).take(2)
    return if (words.isEmpty()) "?" 
           else words.joinToString("") { it.take(1).uppercase() }
}

private fun getAvatarColor(name: String): Color {
    val colors = listOf(
        Color(0xFF3B82F6), // Blue
        Color(0xFFEF4444), // Red
        Color(0xFF10B981), // Green
        Color(0xFFF59E0B), // Amber
        Color(0xFF8B5CF6), // Purple
        Color(0xFFEC4899), // Pink
        Color(0xFF06B6D4)  // Cyan
    )
    val index = kotlin.math.abs(name.hashCode()) % colors.size
    return colors[index]
}
