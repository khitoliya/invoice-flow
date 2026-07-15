package com.dollyplastic.invoiceapp.ui.screens.processing

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.ValueCallback
import android.webkit.WebView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dollyplastic.invoiceapp.data.models.InvoiceStatus
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.ui.screens.processing.components.PortalDownloadHandler
import com.dollyplastic.invoiceapp.ui.screens.processing.components.PortalWebView
import java.io.File



@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortalScreen(
    invoiceId: String,
    url: String,
    storageRef: InvoiceStorageRef,
    credentials: List<com.dollyplastic.invoiceapp.data.credentials.Credential> = emptyList(), // Pass directly
    isCancellationFlow: Boolean = false,
    portalMode: String = "EWAY", // "EWAY" or "EINVOICE"
    viewModel: InvoiceProcessingViewModel = hiltViewModel(),
    onClose: () -> Unit,
    onDownloadSuccess: (File) -> Unit
) {

    // ... (rest of state code)
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    
    // ... [existing code for download handler and webview]

    val downloader = remember {
        PortalDownloadHandler(
            context = context.applicationContext,
            invoiceNumber = invoiceId,
            storageRef = storageRef,
            onFileReady = { file ->
                onDownloadSuccess(file)
            }
        )
    }


    // PortalWebView State
    var uploadMessage: ValueCallback<Array<Uri>>? by remember { mutableStateOf(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Load Data into this Screen's ViewModel instance
    LaunchedEffect(invoiceId) {
        // We only really need the invoice metadata to load credentials
        viewModel.loadInvoice(invoiceId, autoStart = false)
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                uploadMessage?.onReceiveValue(arrayOf(uri))
            } else {
                uploadMessage?.onReceiveValue(null)
            }
        } else {
            uploadMessage?.onReceiveValue(null)
        }
        uploadMessage = null
    }
    
    // ... (Events effect)

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is InvoiceProcessingUiEvent.NavigateBack -> onClose()
                is InvoiceProcessingUiEvent.NavigateToPortal -> {}
                is InvoiceProcessingUiEvent.ViewPdf -> {}
                is InvoiceProcessingUiEvent.SharePdf -> {}
                is InvoiceProcessingUiEvent.NavigateToEdit -> {}
                is InvoiceProcessingUiEvent.InvoiceDeleted -> {}
                is InvoiceProcessingUiEvent.ShowError -> {}
            }
        }
    }


    // Manual Upload Launcher (Failsafe for broken downloads)
    val manualUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        // ... (existing manual upload code)
        if (uri != null) {
            val invoiceDir = InvoiceStorage.getTemporaryDirectory(
                storageRef.firmName,
                storageRef.invoiceNumber
            )
            // Ensure dir exists
            if (!invoiceDir.exists()) invoiceDir.mkdirs()

            // Determine name/extension
             val resultName = "Manual_Source_Document.pdf" // Default to PDF as suggested
             val destFile = File(invoiceDir, resultName)
             
             try {
                 context.contentResolver.openInputStream(uri)?.use { input ->
                     destFile.outputStream().use { output ->
                         input.copyTo(output)
                     }
                 }
                 onDownloadSuccess(destFile)
             } catch (e: Exception) {
                 android.widget.Toast.makeText(context, "Failed to copy file: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
             }
        }
    }

    // Verification Sheet State
    var pendingScrapeList by remember { mutableStateOf<List<Map<String, String>>?>(null) }
    
    // JS Scraper Logic (UPDATED for Multi-Row + JSON + MODE SEPARATION)
    val triggerScrape = {
        val jsScraper = if (portalMode == "EWAY") {
             """
            (function() {
                function normalize(str) {
                    if (!str) return '';
                    return str.toLowerCase().replace(/[\s\.]/g, '');
                }

                function parseBulkUploadTable() {
                    var bulkTable = document.getElementById('ctl00_ContentPlaceHolder1_BulkEwayBills');
                    if (!bulkTable) return null;
                    
                    var headersNodes = document.querySelectorAll('th');
                    var headers = Array.from(headersNodes).map(th => th.innerText.trim());
                    
                    var data = [];
                    var rows = Array.from(bulkTable.querySelectorAll('tr'));
                    
                    rows.forEach(tr => {
                        var cells = Array.from(tr.querySelectorAll('td'));
                        if (cells.length > 0) {
                             var map = {};
                             headers.forEach((h, i) => {
                                 if (cells[i]) map[normalize(h)] = cells[i].innerText.trim();
                             });
                             var res = {};
                             
                             // Standardize according to fuzzy keys mapped to standard schema
                             res['E-Way Bill No'] = map['ewbno'] || map['ewaybillno'] || '';
                             res['E-Way Bill Date'] = map['ewddate'] || map['ewbdate'] || map['date'] || '';
                             res['Valid Until'] = map['validtilldate'] || map['validtill'] || '';
                             res['Doc. No'] = map['docno'] || map['invoiceno'] || '';
                             res['Generated By'] = ''; // Left blank intentionally for Bulk
                             
                             if (res['E-Way Bill No'] !== '') {
                                 data.push(res);
                             }
                        }
                    });
                    return data.length > 0 ? data : null;
                }

                function parseTable() {
                    var tables = Array.from(document.querySelectorAll('table'));
                    
                    // Logic: Find table with EWB No header using looser match
                    var table = tables.find(t => {
                        var text = t.innerText.toLowerCase();
                        return text.includes('ewb') && (text.includes('date') || text.includes('no'));
                    });

                    if (!table) return [];

                    var data = [];
                    var headers = Array.from(table.querySelectorAll('thead th')).map(th => th.innerText.trim());
                    var rows = Array.from(table.querySelectorAll('tbody tr'));
                    
                    rows.forEach(tr => {
                        var cells = Array.from(tr.querySelectorAll('td'));
                        if (cells.length > 0) {
                             var map = {};
                             headers.forEach((h, i) => {
                                 if (cells[i]) map[h] = cells[i].innerText.trim();
                             });
                             var res = {};
                             for (var k in map) res[k] = map[k];
                             
                             // Standardize for E-Way
                             res['E-Way Bill No'] = map['EWB No'] || map['EWB.No'] || map['E-Way Bill No'] || '';
                             res['E-Way Bill Date'] = map['EWB Date'] || map['Date'] || '';
                             res['Valid Until'] = map['Valid Till Date'] || '';
                             res['Doc. No'] = map['Doc. No'] || map['Invoice No'] || map['Doc No'] || '';
                             res['Generated By'] = (map['From GSTIN'] || '') + ' - ' + (map['From GSTIN Info'] || '');
                             
                             data.push(res);
                        }
                    });
                    
                    return data;
                }

                var result = parseBulkUploadTable();
                if (!result || result.length === 0) {
                    result = parseTable();
                }
                var json = JSON.stringify(result);
                window.Android.processHtml("JSON_START" + json + "JSON_END");
            })();
        """.trimIndent()
        } else if (portalMode == "EINVOICE_AND_EWAY") {
             // COMBINED SCRAPER (E-Invoice Portal)
             """
            (function() {
                function parseTable() {
                    var tables = Array.from(document.querySelectorAll('table'));
                    
                    var table = tables.find(t => {
                        var text = t.innerText.toLowerCase();
                        return text.includes('irn') || text.includes('ack. no');
                    });

                    if (!table) return [];

                    var data = [];
                    var headers = Array.from(table.querySelectorAll('thead th')).map(th => th.innerText.trim());
                    var rows = Array.from(table.querySelectorAll('tbody tr'));
                    
                    rows.forEach(tr => {
                        var cells = Array.from(tr.querySelectorAll('td'));
                        if (cells.length > 0) {
                             var map = {};
                             headers.forEach((h, i) => {
                                 if (cells[i]) map[h] = cells[i].innerText.trim();
                             });
                             var res = {};
                             for (var k in map) res[k] = map[k];
                             
                             // Standardize Combined
                             res['IRN'] = map['IRN'] || '';
                             res['Ack No'] = map['Ack. No'] || map['Ack No'] || '';
                             res['Ack Date'] = map['Ack. Date'] || map['Ack Date'] || '';
                             res['Doc. No'] = map['Doc. No'] || map['Invoice No'] || map['Doc No'] || '';
                             res['Status'] = map['Status'] || '';
                             
                             // E-Way Fields (often present in E-Invoice Dashboard)
                             res['E-Way Bill No'] = map['EWB No.'] || map['EWB No'] || map['E-Way Bill No'] || map['Eway Bill No'] || '';
                             res['E-Way Bill Date'] = map['EWB Date'] || map['E-Way Bill Date'] || '';
                             res['Valid Until'] = map['Valid Till'] || map['EWB Valid Till'] || '';
                             
                             data.push(res);
                        }
                    });
                    
                    return data;
                }

                var result = parseTable();
                var json = JSON.stringify(result);
                window.Android.processHtml("JSON_START" + json + "JSON_END");
            })();
        """.trimIndent()
        } else {
            // E-INVOICE ONLY SCRAPER
             """
            (function() {
                function parseTable() {
                    var tables = Array.from(document.querySelectorAll('table'));
                    
                    // Logic: Find table with IRN or Ack No
                    var table = tables.find(t => {
                        var text = t.innerText.toLowerCase();
                        return text.includes('irn') || text.includes('ack. no') || text.includes('invoice no');
                    });

                    if (!table) return [];

                    var data = [];
                    var headers = Array.from(table.querySelectorAll('thead th')).map(th => th.innerText.trim());
                    var rows = Array.from(table.querySelectorAll('tbody tr'));
                    
                    rows.forEach(tr => {
                        var cells = Array.from(tr.querySelectorAll('td'));
                        if (cells.length > 0) {
                             var map = {};
                             headers.forEach((h, i) => {
                                 if (cells[i]) map[h] = cells[i].innerText.trim();
                             });
                             var res = {};
                             for (var k in map) res[k] = map[k];
                             
                             // Standardize for E-Invoice Only
                             res['IRN'] = map['IRN'] || '';
                             res['Ack No'] = map['Ack. No'] || map['Ack No'] || '';
                             res['Ack Date'] = map['Ack. Date'] || map['Ack Date'] || '';
                             res['Doc. No'] = map['Doc. No'] || map['Invoice No'] || map['Doc No'] || '';
                             res['Status'] = map['Status'] || '';
                             
                             data.push(res);
                        }
                    });
                    
                    return data;
                }

                var result = parseTable();
                var json = JSON.stringify(result);
                window.Android.processHtml("JSON_START" + json + "JSON_END");
            })();
        """.trimIndent()
        }
        
        webViewRef?.evaluateJavascript(jsScraper, null)
    }
    
    // ... (Rest of Scaffold)
    // Update Verification Sheet Call to pass mode
    // (Wait, I need to see where Scaffold content is... it was truncated in previous view. I'll read file first to be safe or just apply replace to ScrapeVerificationSheet content logic)
    // Since I don't see the ScrapeVerificationLogic calling code in "Replacement Content", I just did the Scraper part.
    // I need to find where ScrapeVerificationSheet is called. It's likely in the Scaffold content.
    // I will replace the Scaffold content in a second step or if visible.
    // The previous view_file of PortalScreen showed up to line ... wait, I haven't viewed PortalScreen in THIS turn, only in history.
    // I will do the Header/Scraper update now. Then the Bottom Sheet update.




    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isCancellationFlow) "Cancel on Portal" else "Government Portal") },
                navigationIcon = {
                     IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    if (!isCancellationFlow) {
                        TextButton(onClick = { triggerScrape() }) {
                             Text("Import Details")
                        }
                        TextButton(onClick = {
                            manualUploadLauncher.launch(arrayOf("application/pdf", "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                        }) {
                             Text("Select File")
                        }
                    }
                }
            )
        }
    ) { padding ->
        // SPLIT VIEW LAYOUT
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            
            // 1. TOP HALF: WEBVIEW
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(if (pendingScrapeList != null) 0.5f else 1f) // Shrinks to 50% when verifying
            ) {
                 PortalWebView(
                    url = url,
                    credentials = credentials,
                    firmIdentifier = storageRef.firmName,
                    portalMode = portalMode,
                    onHtmlScraped = { content ->
                        // Parse JSON Envelope
                        try {
                            if (content.startsWith("JSON_START")) {
                                val jsonStr = content.substringAfter("JSON_START").substringBefore("JSON_END")
                                val typeToken = object : com.google.gson.reflect.TypeToken<List<Map<String, String>>>() {}.type
                                val list: List<Map<String, String>> = com.google.gson.Gson().fromJson(jsonStr, typeToken)
                                
                                if (list.isNotEmpty()) {
                                    pendingScrapeList = list
                                } else {
                                    android.widget.Toast.makeText(context, "No table data found to import.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                // Fallback or Error
                                android.util.Log.w("PortalScreen", "Scraper returned non-JSON content: $content")
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("PortalScreen", "JSON Parse Error", e)
                             android.widget.Toast.makeText(context, "Scrape Error: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    },
                     onShowFileChooser = { callback ->
                         uploadMessage = callback
                         
                         val invoiceDir = InvoiceStorage.getTemporaryDirectory(storageRef.firmName, storageRef.invoiceNumber)
                         val basePath = Environment.getExternalStorageDirectory().absolutePath
                         val relativePath = invoiceDir.absolutePath.substringAfter(basePath).removePrefix("/")
                         val documentUri = android.provider.DocumentsContract.buildDocumentUri(
                             "com.android.externalstorage.documents",
                             "primary:$relativePath"
                         )
                         
                         val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply { 
                             addCategory(Intent.CATEGORY_OPENABLE)
                             type = "*/*"
                             putExtra(android.provider.DocumentsContract.EXTRA_INITIAL_URI, documentUri) 
                         }
                         filePickerLauncher.launch(intent)
                         true
                     },
                     onDownload = { url, userAgent, contentDisposition, mimetype, _ ->
                         downloader.startDownload(url, userAgent, contentDisposition, mimetype)
                     },
                    onWebViewCreated = { webViewRef = it },
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            // 2. BOTTOM HALF: VERIFICATION PANE
            if (pendingScrapeList != null) {
                Divider(thickness = 2.dp, color = MaterialTheme.colorScheme.outline) // Visual separator
                
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.5f)
                        .padding(bottom = 0.dp) // Reset padding
                ) {
                    com.dollyplastic.invoiceapp.ui.screens.processing.components.ScrapeVerificationSheet(
                        dataList = pendingScrapeList!!,
                        targetInvoiceNumber = storageRef.invoiceNumber,
                        portalMode = portalMode, // Pass Mode
                        onCancel = { pendingScrapeList = null }, // Close Split View
                        onConfirm = { selectedMap ->
                            // Save logic
                             val content = if (portalMode == "EWAY") {
                                 val ewb = selectedMap["E-Way Bill No"] ?: ""
                                 val date = selectedMap["E-Way Bill Date"] ?: ""
                                 val valid = selectedMap["Valid Until"] ?: ""
                                 val validFrom = selectedMap["Valid From"] ?: ""
                                 val genBy = selectedMap["Generated By"] ?: ""
                                 
                                 "E-Way Bill No: $ewb\n" +
                                 "E-Way Bill Date: $date\n" +
                                 "Valid Until: $valid\n" +
                                 "Valid From: $validFrom\n" +
                                 "Generated By: $genBy\n"
                             } else if (portalMode == "EINVOICE_AND_EWAY") {
                                 val irn = selectedMap["IRN"] ?: ""
                                 val ackNo = selectedMap["Ack No"] ?: ""
                                 val ackDate = selectedMap["Ack Date"] ?: ""
                                 val status = selectedMap["Status"] ?: ""
                                 val ewb = selectedMap["E-Way Bill No"] ?: ""
                                 val ewbDate = selectedMap["E-Way Bill Date"] ?: ""
                                 val valid = selectedMap["Valid Until"] ?: ""
                                 val validFrom = selectedMap["Valid From"] ?: ""
                                 val genBy = selectedMap["Generated By"] ?: ""
                                 
                                 "IRN: $irn\n" +
                                 "Ack No: $ackNo\n" +
                                 "Ack Date: $ackDate\n" +
                                 "Status: $status\n" +
                                 "E-Way Bill No: $ewb\n" +
                                 "E-Way Bill Date: $ewbDate\n" +
                                 "Valid Until: $valid\n" +
                                 "Valid From: $validFrom\n" +
                                 "Generated By: $genBy\n"
                             } else {
                                 val irn = selectedMap["IRN"] ?: ""
                                 val ackNo = selectedMap["Ack No"] ?: ""
                                 val ackDate = selectedMap["Ack Date"] ?: ""
                                 val status = selectedMap["Status"] ?: ""
                                 
                                 "IRN: $irn\n" +
                                 "Ack No: $ackNo\n" +
                                 "Ack Date: $ackDate\n" +
                                 "Status: $status\n"
                             }
                                           
                             saveScrapedContent(context, storageRef, content, onDownloadSuccess)
                             pendingScrapeList = null
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        
        }
    }
}

private fun saveScrapedContent(
    context: android.content.Context, 
    storageRef: InvoiceStorageRef, 
    content: String, 
    onSuccess: (File) -> Unit
) {
    val invoiceDir = InvoiceStorage.getTemporaryDirectory(
        storageRef.firmName,
        storageRef.invoiceNumber
    )
    // Ensure exists
    if (!invoiceDir.exists()) invoiceDir.mkdirs()
    
    val resultFile = File(invoiceDir, "Scraped_Data.txt")
    try {
        resultFile.writeText(content)
        onSuccess(resultFile)
    } catch(e: Exception) {
        android.util.Log.e("InvoiceWorkflow", "Failed to save scraped content", e)
        android.widget.Toast.makeText(context, "Import Failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
