package com.dollyplastic.invoiceapp.ui.screens.masters.item


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.MoreVert

import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.data.models.Item
import com.dollyplastic.invoiceapp.ui.common.dialogs.WarningDialog
import com.dollyplastic.invoiceapp.ui.navigation.Route
import com.dollyplastic.invoiceapp.ui.theme.AppColors
import com.dollyplastic.invoiceapp.ui.common.Animations.SmoothExpansion

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ItemListScreen(
    navController: NavController,
    viewModel: ItemViewModel = hiltViewModel()
) {
    val items by viewModel.itemUiModels.collectAsState()
    val availableFirms by viewModel.availableFirms.collectAsState()
    val selectedFirm by viewModel.selectedFirm.collectAsState()

    var deleteItem by remember { mutableStateOf<ItemUiModel?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState() // For scrolling

    // Filtered List
    val filteredItems = remember(items, searchQuery) {
        if (searchQuery.isBlank()) items
        else items.filter {
            it.item.name.contains(searchQuery, ignoreCase = true) ||
                    it.item.hsnCode.contains(searchQuery, ignoreCase = true)
        }
    }
    
    // Ensure mock data is refreshed once firms are loaded
    LaunchedEffect(availableFirms) {
        if(availableFirms.isNotEmpty()) {
             viewModel.refreshMockStock()
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                // Back Button
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBackIosNew,
                        contentDescription = "Back",
                        tint = Color.Black
                    )
                }

                // Title
                Text(
                    text = "Items",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    ),

                    textAlign = TextAlign.Center
                )

                // Divider
                HorizontalDivider(
                    thickness = 1.dp,
                    color = AppColors.Border,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        },
    ) { padding ->


        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // Sticky Search & Action Header
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
                    // Search & Add Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Search Bar
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AppColors.InputBackground,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = AppColors.TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier.weight(1f),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            text = "Search items",
                                            style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.TextSecondary, fontSize =18.sp, fontWeight = FontWeight.Light),
                                            modifier = Modifier.align(Alignment.CenterStart)
                                        )
                                    }
                                    BasicTextField(
                                        value = searchQuery,
                                        onValueChange = { searchQuery = it },
                                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.Black),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth().align(Alignment.CenterStart),
                                        decorationBox = { innerTextField ->
                                            Box(contentAlignment = Alignment.CenterStart) {
                                                innerTextField()
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Add Button
                        Button(
                            onClick = { navController.navigate(Route.ItemForm.create()) },
                            colors = buttonColors(
                                containerColor = AppColors.PrimaryBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Add Item",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }

                    // Filters Row
                    Row(
                         modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                         horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                         // Dummy Filter/Sort Buttons for UI completeness
                         FilterChip(
                             selected = false,
                             onClick = { },
                             label = { Text("Filter") },
                             leadingIcon = { Icon(Icons.Default.FilterList, null, modifier = Modifier.size(16.dp)) }
                         )
                         FilterChip(
                             selected = false,
                             onClick = { },
                             label = { Text("Sort") },
                             leadingIcon = { Icon(Icons.Default.Sort, null, modifier = Modifier.size(16.dp)) }
                         )

                         // Firm Filter Dropdown
                         var firmMenuExpanded by remember { mutableStateOf(false) }
                         Box {
                             FilterChip(
                                 selected = selectedFirm != null,
                                 onClick = { firmMenuExpanded = true },
                                 label = {
                                     Text(
                                         text = selectedFirm?.nickName ?: "All Firms",
                                         maxLines = 1,
                                         overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                         modifier = Modifier.widthIn(max = 120.dp)
                                     )
                                 },
                                 trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                             )
                             DropdownMenu(
                                 expanded = firmMenuExpanded,
                                 onDismissRequest = { firmMenuExpanded = false }
                             ) {
                                 DropdownMenuItem(
                                     text = { Text("All Firms") },
                                     onClick = {
                                         viewModel.selectFirm(null)
                                         firmMenuExpanded = false
                                     }
                                 )
                                 availableFirms.forEach { firm ->
                                      DropdownMenuItem(
                                         text = { Text(firm.nickName) },
                                         onClick = {
                                             viewModel.selectFirm(firm)
                                             firmMenuExpanded = false
                                         }
                                     )
                                 }
                             }
                         }
                    }
                    Divider(color = AppColors.Border)
                }
            }

            items(filteredItems) { uiModel ->
                ItemCard(
                    uiModel = uiModel,
                    selectedFirm = selectedFirm,
                    onExpand = { viewModel.toggleItemExpansion(uiModel.item.itemId) },
                    onEdit = { navController.navigate(Route.ItemForm.create(uiModel.item.itemId)) },
                    onDelete = { deleteItem = uiModel }
                )
            }
        }
    }

    // Delete confirmation dialog
    deleteItem?.let { uiModel ->
        WarningDialog(
            title = "Warning",
            description = "Are you sure you want to delete ${uiModel.item.name}?\nThis action cannot be undone and all associated data will be permanently removed.",
            onConfirm = {
                viewModel.deleteItem(uiModel.item.itemId)
                deleteItem = null
            },
            onDismiss = { deleteItem = null }
        )
    }

}

@Composable
fun ItemCard(
    uiModel: ItemUiModel,
    selectedFirm: com.dollyplastic.invoiceapp.data.models.Firm?,
    onExpand: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, AppColors.Border),
        modifier = Modifier.fillMaxWidth().clickable {
            // Only toggle expansion if "All Firms" is selected (i.e. selectedFirm is null)
            if(selectedFirm == null) {
                onExpand()
            }
        }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Row 1: Icon + Name + Total Stock
            Row(verticalAlignment = Alignment.Top) {
                 // Placeholder Icon
                 Surface(
                     shape = RoundedCornerShape(8.dp),
                     color = AppColors.PrimaryBlue.copy(alpha = 0.1f),
                     modifier = Modifier.size(40.dp)
                 ) {
                     Box(contentAlignment = Alignment.Center) {
                         // Use first letter as icon
                         Text(
                             text = uiModel.item.name.take(1).uppercase(),
                             style = MaterialTheme.typography.titleMedium.copy(color = AppColors.PrimaryBlue)
                         )
                     }
                 }
                 Spacer(modifier = Modifier.width(12.dp))
                 
                 Column(modifier = Modifier.weight(1f)) {
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.SpaceBetween,
                         verticalAlignment = Alignment.Top
                     ) {
                         Text(
                             text = uiModel.item.name,
                             style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                             modifier = Modifier.weight(1f)
                         )
                         // 3-Dot Menu / Actions
                         // Using Row for now as simple actions
                         // 3-Dot Menu
                         Box {
                             var expanded by remember { mutableStateOf(false) }
                             IconButton(
                                 onClick = { expanded = true },
                                 modifier = Modifier.size(24.dp)
                             ) {
                                 Icon(
                                     Icons.Default.MoreVert,
                                     contentDescription = "Options",
                                     tint = AppColors.TextSecondary,
                                     modifier = Modifier.size(20.dp)
                                 )
                             }
                             DropdownMenu(
                                 expanded = expanded,
                                 onDismissRequest = { expanded = false },
                                 modifier = Modifier.background(Color.White)
                             ) {
                                 DropdownMenuItem(
                                     text = { Text("Edit") },
                                     onClick = {
                                         expanded = false
                                         onEdit()
                                     },
                                     leadingIcon = {
                                         Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(20.dp))
                                     }
                                 )
                                 DropdownMenuItem(
                                     text = { Text("Delete", color = Color.Red) },
                                     onClick = {
                                         expanded = false
                                         onDelete()
                                     },
                                     leadingIcon = {
                                         Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                     }
                                 )
                             }
                         }
                     }
                     Spacer(modifier = Modifier.height(4.dp))
                     
                     // Metadata Row
                     Row(verticalAlignment = Alignment.CenterVertically) {
                         MetadataChip(text = "HSN: ${uiModel.item.hsnCode}")
                         Spacer(modifier = Modifier.width(6.dp))
                         MetadataChip(text = "Tax: ${uiModel.item.gstRate.toInt()}%")
                         Spacer(modifier = Modifier.width(6.dp))
                         MetadataChip(text = "Unit: ${uiModel.item.unit}")
                     }
                 }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = AppColors.Border.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Total Stock Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Text(
                     text = if(selectedFirm == null) "Total Stock" else "Stock in ${selectedFirm.nickName}",
                     style = MaterialTheme.typography.bodyMedium.copy(color = AppColors.TextSecondary)
                 )
                 Text(
                     text = "${uiModel.totalStock} ${uiModel.item.unit}",
                     style = MaterialTheme.typography.titleMedium.copy(
                         color = AppColors.PrimaryBlue,
                         fontWeight = FontWeight.Bold
                     )
                 )
            }
            
            // Expanded Section (Firm Details)
            // Expanded Section (Firm Details)
            SmoothExpansion(visible = uiModel.isExpanded && selectedFirm == null) {
                Column { // Add Column to ensure content structure for animation
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = AppColors.Background,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Firm-wise Stock",
                                style = MaterialTheme.typography.labelMedium.copy(color = AppColors.TextSecondary),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            uiModel.firmStocks.forEach { firmStock ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = firmStock.firmName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${firmStock.stockQty} ${uiModel.item.unit}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataChip(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall.copy(
            color = AppColors.TextSecondary,
            fontSize = 11.sp
        )
    )
}
