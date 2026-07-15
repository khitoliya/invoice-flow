package com.dollyplastic.invoiceapp.ui.common.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dollyplastic.invoiceapp.ui.theme.AppColors

@Composable
fun ValidationErrorDialog(
    errors: List<String>,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.padding(24.dp)
            ) {
                // Close Icon (Top Right)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.LightGray
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Red Exclamation Icon
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFDC2626), // Red 600
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PriorityHigh,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Validation Error",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937), // Gray 800
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Please correct the errors before proceeding:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF4B5563) // Gray 600
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Error List
                    Column(
                        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        errors.forEach { error ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp) // Condensed bullet
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("!", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = Color(0xFF374151)
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3B82F6), // Blue 500
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("OK", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
fun WarningDialog(
    title: String,
    description: String,
    confirmText: String = "Delete",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            tonalElevation = 6.dp
        ) {
            Box(
                modifier = Modifier.padding(24.dp)
            ) {
                 // Close Icon (Top Right)
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd).offset(x = 12.dp, y = (-12).dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.LightGray
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Triangle Icon (Simulated with Box/Icon for now, reusing Exclamation)
                    // Visual spec asks for Warning icon.
                    // Using standard warning icon which is usually a triangle.
                     Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEE2E2), // Red 100
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                             Icon(
                                imageVector = Icons.Default.Warning, 
                                contentDescription = null,
                                tint = Color(0xFFDC2626), // Red 600
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    // For a true triangle, we'd draw it, but standard Icon is safer for now.
                    // The user image shows a red triangle. Let's try to find a system icon or just use the red circle pattern but maybe triangle vector if available.
                    // Icons.Default.Warning is a Triangle with ! inside.
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title, // "Warning"
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            fontSize = 20.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF6B7280)
                        ),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF374151)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD1D5DB))
                        ) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Button(
                            onClick = onConfirm,
                            modifier = Modifier.weight(1f).height(48.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFDC2626), // Red 600
                                contentColor = Color.White
                            )
                        ) {
                            Text(confirmText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
