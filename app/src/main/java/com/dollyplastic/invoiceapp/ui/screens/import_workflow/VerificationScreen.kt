package com.dollyplastic.invoiceapp.ui.screens.import_workflow

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dollyplastic.invoiceapp.ui.components.PdfViewer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.background

@Composable
fun VerificationScreen(
    uri: String,
    onNavigateBack: () -> Unit,
    viewModel: ImportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.parse(uri)
    }

    LaunchedEffect(uiState) {
        if (uiState is ImportUiState.Success) {
            onNavigateBack() // Or navigate to success screen
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (val state = uiState) {
            is ImportUiState.Idle, is ImportUiState.Parsing -> {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ImportUiState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is ImportUiState.ParsedPurchase -> {
                SplitViewContent(
                    pdfPath = state.purchase.pdfFilePath ?: "",
                    formContent = {
                        VerificationForm(
                             initialPurchase = state.purchase,
                             onSave = { updatedPurchase -> viewModel.savePurchase(updatedPurchase) }
                        )
                    },
                    onBack = onNavigateBack
                )
            }
            else -> {}
        }
    }
}

@Composable
fun SplitViewContent(
    pdfPath: String,
    formContent: @Composable () -> Unit,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Top: PDF (Weight 1f)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            PdfViewer(
                pdfPath = pdfPath,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay Back Button
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .padding(8.dp)
                    .align(androidx.compose.ui.Alignment.TopStart)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        
        // Divider
        HorizontalDivider(thickness = 4.dp, color = MaterialTheme.colorScheme.outlineVariant)

        // Bottom: Form (Weight 1f)
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            formContent()
        }
    }
}
