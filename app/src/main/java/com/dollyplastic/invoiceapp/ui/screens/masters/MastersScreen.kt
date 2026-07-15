package com.dollyplastic.invoiceapp.ui.screens.masters

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastersScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Masters",
                        style = MaterialTheme.typography.headlineMedium.copy( fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    )
                        },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color.White // User requested white scaffold
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ITEM MASTER
            MasterCard(
                title = "Item Master",
                subtitle = "Manage products",
                icon = Icons.Outlined.Inventory2, // Box icon
                color = Color(0xFF1E6091), // Deep Blue
                iconBg = Color(0xFFE0F2FE), // Pale Blue
                onViewClick = { navController.navigate(Route.ItemList.route) },
                onAddClick = { navController.navigate(Route.ItemForm.create()) },
                addLabel = "Add Product",
                viewLabel = "View Products"
            )

            // PARTY MASTER
            MasterCard(
                title = "Party Master",
                subtitle = "Manage clients",
                icon = Icons.Outlined.Groups,
                color = Color(0xFFEA580C), // Burnt Orange
                iconBg = Color(0xFFFFF7ED), // Pale Orange
                onViewClick = { navController.navigate(Route.PartyList.route) },
                onAddClick = { navController.navigate(Route.PartyForm.create()) },
                addLabel = "Add Client",
                viewLabel = "View Clients"
            )

            // FIRM MASTER
            MasterCard(
                title = "Firm Master",
                subtitle = "Manage your firms",
                icon = Icons.Outlined.Business,
                color = Color(0xFF059669), // Emerald
                iconBg = Color(0xFFECFDF5), // Pale Green
                onViewClick = { navController.navigate(Route.FirmList.route) },
                onAddClick = { navController.navigate(Route.FirmForm.create()) },
                addLabel = "Add Firm",
                viewLabel = "View Firms"
            )
        }
    }
}

@Composable
fun MasterCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    iconBg: Color,
    onViewClick: () -> Unit,
    onAddClick: () -> Unit,
    addLabel: String,
    viewLabel: String
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // --- HEADER SECTION (Tinted) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(iconBg.copy(alpha = 0.35f)) // Lighter tint
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Divider between tinted header and white body
            HorizontalDivider(thickness = 1.dp, color = Color(0xFFF3F4F6))

            // --- ACTION SECTION (White) ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Add Button (Slight Elevation)
                Button(
                    onClick = onAddClick,
                    colors = ButtonDefaults.buttonColors(

                        containerColor = iconBg, 
                        contentColor = color
                    ),

                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(addLabel)
                }

                // 2. View Button
                OutlinedButton(
                    onClick = onViewClick,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF374151)
                    ),

                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                   Icon(Icons.Outlined.ListAlt, null, modifier = Modifier.size(18.dp), tint = color)
                   Spacer(modifier = Modifier.width(8.dp))
                   Text(viewLabel)
                }
                
                // 3. Import (Placeholder)
                OutlinedButton(
                    onClick = { 
                        Toast.makeText(context, "Upcoming", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF374151)
                    ),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                   Icon(Icons.Outlined.Download, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6B7280))
                   Spacer(modifier = Modifier.width(8.dp))
                   Text("Import")
                }
                
               // 4. Config (Placeholder)
                OutlinedButton(
                    onClick = { 
                        Toast.makeText(context, "Upcoming", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF374151)
                    ),

                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                   Icon(Icons.Outlined.Settings, null, modifier = Modifier.size(18.dp), tint = Color(0xFF6B7280))
                   Spacer(modifier = Modifier.width(8.dp))
                   Text("Config")
                }
            }
        }
    }
}
