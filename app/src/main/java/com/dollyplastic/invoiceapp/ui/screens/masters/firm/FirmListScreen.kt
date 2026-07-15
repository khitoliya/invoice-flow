package com.dollyplastic.invoiceapp.ui.screens.masters.firm

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.dollyplastic.invoiceapp.data.models.Firm
import com.dollyplastic.invoiceapp.ui.navigation.Route
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.buttonColors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dollyplastic.invoiceapp.ui.common.buttons.PrimaryButton
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartySeedData
import com.dollyplastic.invoiceapp.ui.theme.AppColors


@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun FirmListScreen(
    navController: NavController,
    viewModel: FirmViewModel = hiltViewModel()
) {
    val firms by viewModel.firms.collectAsState()
    var firmToDelete by remember { mutableStateOf<Firm?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    val listState = rememberLazyListState() // For scrolling
    
    // Filtered List
    val filteredFirms = remember(firms, searchQuery) {
        if (searchQuery.isBlank()) firms
        else firms.filter { 
            it.tradeName.contains(searchQuery, ignoreCase = true) ||
                    it.nickName.contains(searchQuery, ignoreCase = true)||
                    it.gstin.contains(searchQuery, ignoreCase = true)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadFirms()
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
                    text = "Firms",
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
        containerColor = Color.White
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = padding.calculateTopPadding()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {



            // Sticky Search & Action Header
            stickyHeader {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                ) {
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
                                            text = "Search firms",
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
                            onClick = { navController.navigate(Route.FirmForm.create()) },
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
                                text = "Add Firm",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                    HorizontalDivider(color = AppColors.Border, thickness = 1.dp)
                }
            }







            items(filteredFirms) { firm ->
                com.dollyplastic.invoiceapp.ui.common.list.EntityListCard(
                    tradeName = firm.tradeName,
                    city = firm.city,
                    state = firm.state,
                    gstin = if (firm.gstin.isNotBlank()) "GSTIN: ${firm.gstin}" else "Unregistered",
                    nickName = firm.nickName,
                    fullAddress = listOf(firm.addressLine1, firm.addressLine2 ?: "", "${firm.city} - ${firm.pincode}").filter { it.isNotBlank() }.joinToString(", "),
                    bankDetails = if (firm.bankName.isNotBlank()) "${firm.bankName}\nAcc: ${firm.accountNumber}\nIFSC: ${firm.ifscCode}" else null,
                    onEdit = {
                        navController.navigate(Route.FirmForm.create(firm.firmId))
                    },
                    onDelete = {
                        firmToDelete = firm
                    },
                    modifier = Modifier
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    firmToDelete?.let { firm ->
        com.dollyplastic.invoiceapp.ui.common.dialogs.WarningDialog(
            title = "Warning",
            description = "Are you sure you want to delete this firm?\nThis action cannot be undone and all associated data will be permanently removed.",
            onConfirm = {
                viewModel.deleteFirm(firm.firmId)
                firmToDelete = null
            },
            onDismiss = { firmToDelete = null }
        )
    }
}
