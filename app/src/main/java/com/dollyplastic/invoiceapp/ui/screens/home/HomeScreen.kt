package com.dollyplastic.invoiceapp.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import kotlinx.coroutines.launch
import com.dollyplastic.invoiceapp.utils.DevUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToCreate: () -> Unit,
    onNavigateToProcessing: (String) -> Unit,
    onNavigateToPortal: (String, String, InvoiceStorageRef, String, Boolean, String) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val filters by viewModel.filterState.collectAsState()
    val firms by viewModel.firms.collectAsState()
    val stockItems by viewModel.stockState.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isFixing by viewModel.isFixingInvoices.collectAsState()
    val fixProgress by viewModel.fixProgress.collectAsState()
    val isGenerating by viewModel.isGeneratingDummyInvoices.collectAsState()
    val progress by viewModel.dummyInvoiceProgress.collectAsState()
    
    val scope = rememberCoroutineScope()
    
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                 viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // 1. Header Title & Settings
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Invoice App",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. Dashboard Card
            item {
                com.dollyplastic.invoiceapp.ui.screens.home.dashboard.FirmDashboardCard(
                    firms = firms,
                    selectedFirmGstin = filters.selectedFirmGstin,
                    onFirmSelected = viewModel::onFirmSelected,
                    totalSales = summary.second,
                    stockItems = stockItems
                )
            }
            
            item {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.runInvoiceFix() },
                        enabled = !isFixing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text(if (isFixing) "Fixing FY Grouping..." else "Fix Sequence Bug (Run Once)")
                    }
                    
                    if (isFixing) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { fixProgress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Text(
                            text = "${(fixProgress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            // Temporary Dev Button & Progress
            item {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.generateDummyInvoices() },
                        enabled = !isGenerating,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isGenerating) "Generating..." else "Generate Dummy Invoices (from 19)")
                    }
                    
                    if (isGenerating) {
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
