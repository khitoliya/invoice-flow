package com.dollyplastic.invoiceapp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkInfo
import androidx.work.Constraints
import androidx.work.NetworkType
import java.util.concurrent.TimeUnit
import com.dollyplastic.invoiceapp.workers.PdfDriveBackupWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCredentials: () -> Unit,
    onNavigateToRecycleBin: () -> Unit,
    onNavigateToGstConfig: () -> Unit,
    onNavigateToStateConfig: () -> Unit,
    viewModel: SettingsViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val waContact by viewModel.waContact.collectAsState()
    val context = androidx.compose.ui.platform.LocalContext.current
    val workManager = WorkManager.getInstance(context)

    // Google Sign-In setup
    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(Scope(DriveScopes.DRIVE_FILE))
        .build()
    val googleSignInClient = GoogleSignIn.getClient(context, gso)

    val googleSignInLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(com.google.android.gms.common.api.ApiException::class.java)
                viewModel.setDriveConnectedEmail(account?.email)
                android.widget.Toast.makeText(context, "Google Drive Connected", android.widget.Toast.LENGTH_SHORT).show()
                viewModel.setDriveBackupEnabled(true) // Auto-enable on connect
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Sign-in failed", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Contact Picker Launcher
    val contactLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickContact()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            // Permission Check handled by system picker (usually grants temp read access)
            // Resolve Name and Number
            try {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val nameIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts.DISPLAY_NAME)
                        val name = if (nameIndex >= 0) it.getString(nameIndex) else "Unknown"
                        
                        // ID to find phone number
                        val idIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts._ID)
                        val id = if (idIndex >= 0) it.getString(idIndex) else null
                        
                        val hasPhoneIndex = it.getColumnIndex(android.provider.ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        val hasPhone = if (hasPhoneIndex >= 0) it.getString(hasPhoneIndex) else "0"

                        if (hasPhone == "1" && id != null) {
                             val phones = context.contentResolver.query(
                                 android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                 null,
                                 android.provider.ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id,
                                 null,
                                 null
                             )
                             phones?.use { pCursor ->
                                 if (pCursor.moveToFirst()) {
                                     val numberIndex = pCursor.getColumnIndex(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER)
                                     var number = if (numberIndex >= 0) pCursor.getString(numberIndex) else ""
                                     
                                     // Sanitize Number for WhatsApp (Remove spaces, dashes, parentheses)
                                     number = number.replace(Regex("[^0-9+]"), "")
                                     
                                     // Basic check: if starts with '+', keep it. If not, maybe warn? 
                                     // For now, save as is.
                                     
                                     viewModel.setWhatsAppContact(name, number)
                                 }
                             }
                        }
                    }
                }
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Failed to read contact", android.widget.Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission Launcher for READ_CONTACTS (Required for query)
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            contactLauncher.launch(null)
        } else {
             android.widget.Toast.makeText(context, "Contact permission needed to select WhatsApp contact.", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Settings") 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Invisible placeholder to accurately center Title mathematically
                    IconButton(onClick = {}, enabled = false) {
                         Icon(Icons.Default.ArrowBack, contentDescription = null, tint = androidx.compose.ui.graphics.Color.Transparent)
                    }
                }
            )
        }
    ) { padding ->
        val scrollState = rememberScrollState()
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(scrollState)
        ) {
            // Credentials Tile
            SettingsTile(
                title = "Manage Credentials",
                subtitle = "E-Way Bill & E-Invoice Portal Logins",
                icon = Icons.Default.Key,
                onClick = onNavigateToCredentials
            )
            Divider()
            
            // WhatsApp Share Config
            SettingsTile(
                title = "Preferred Share Contact",
                subtitle = waContact?.let { "${it.first} (${it.second})" } ?: "Select a contact for 1-tap WhatsApp sharing",
                icon = androidx.compose.material.icons.Icons.Default.Share,
                onClick = {
                     // Check permission before launching picker
                     if (androidx.core.content.ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                         contactLauncher.launch(null)
                     } else {
                         permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                     }
                }
            )
            if (waContact != null) {
                 TextButton(
                     onClick = { viewModel.clearWhatsAppContact() },
                     modifier = Modifier.padding(start = 56.dp)
                 ) {
                     Text("Clear Preferred Contact", color = MaterialTheme.colorScheme.error)
                 }
            }
            Divider()
            
            // Recycle Bin Tile
            SettingsTile(
                title = "Deleted Invoices",
                subtitle = "View cancelled and archived invoices",
                icon = Icons.Default.Delete,
                onClick = onNavigateToRecycleBin
            )
            Divider()

            // GST Config Tile
            SettingsTile(
                title = "GST Rates",
                subtitle = "Configure allowed GST rates",
                icon = Icons.Default.Settings,
                onClick = onNavigateToGstConfig
            )
            Divider()

            // State Config Tile
            SettingsTile(
                title = "State List",
                subtitle = "Manage Indian States and UTs",
                icon = Icons.Default.LocationOn,
                onClick = onNavigateToStateConfig
            )
            Divider()
            
            // --- Google Drive Backup Section ---
            Text("Backup & Restore", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp))
            
            var showDisconnectDialog by remember { mutableStateOf(false) }

            if (showDisconnectDialog) {
                AlertDialog(
                    onDismissRequest = { showDisconnectDialog = false },
                    title = { Text("Disconnect Google Drive") },
                    text = { Text("Are you sure you want to delink your Google Drive Account? This will immediately stop auto backups.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showDisconnectDialog = false
                            googleSignInClient.signOut().addOnCompleteListener {
                                viewModel.setDriveConnectedEmail(null)
                                viewModel.setDriveBackupEnabled(false)
                            }
                        }) { Text("Disconnect", color = MaterialTheme.colorScheme.error) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDisconnectDialog = false }) { Text("Cancel") }
                    }
                )
            }
            
            // Connect Account
            SettingsTile(
                title = "Google Drive Account",
                subtitle = state.driveConnectedEmail ?: "Tap to connect for Auto-Backup",
                icon = if (state.driveConnectedEmail != null) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                onClick = {
                    if (state.driveConnectedEmail == null) {
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    } else {
                        showDisconnectDialog = true
                    }
                }
            )
            
            if (state.driveConnectedEmail != null) {
                // Folder URL Input
                var folderUrl by remember { mutableStateOf(state.driveFolderUrl) }
                OutlinedTextField(
                    value = folderUrl,
                    onValueChange = { 
                        folderUrl = it
                        viewModel.setDriveFolderUrl(it)
                    },
                    label = { Text("Master Shared Folder URL") },
                    supportingText = { Text("Optional: Paste a shared drive folder link where you have 'Edit' access to directly upload into it.") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    singleLine = true,
                    placeholder = { Text("https://drive.google.com/drive/folders/...") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Toggle Backup
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Auto Daily Backup", style = MaterialTheme.typography.bodyLarge)
                    Switch(
                        checked = state.isDriveBackupEnabled,
                        onCheckedChange = { 
                            viewModel.setDriveBackupEnabled(it) 
                            // Reschedule or Cancel WorkManager
                            if (it) {
                                val constraints = Constraints.Builder().apply {
                                    if (state.driveNetworkType == "WIFI") setRequiredNetworkType(NetworkType.UNMETERED)
                                    else setRequiredNetworkType(NetworkType.CONNECTED)
                                }.build()
                                val workReq = PeriodicWorkRequestBuilder<PdfDriveBackupWorker>(24, TimeUnit.HOURS)
                                    .setConstraints(constraints).build()
                                workManager.enqueueUniquePeriodicWork("DriveAutoBackup", androidx.work.ExistingPeriodicWorkPolicy.UPDATE, workReq)
                            } else {
                                workManager.cancelUniqueWork("DriveAutoBackup")
                            }
                        }
                    )
                }
                
                Divider(modifier = Modifier.padding(horizontal = 16.dp))
                
                // Network Picker
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable { 
                         viewModel.setDriveNetworkType(if (state.driveNetworkType == "WIFI") "ANY" else "WIFI") 
                    },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text("Backup Network", style = MaterialTheme.typography.bodyLarge)
                        Text(if (state.driveNetworkType == "WIFI") "Wi-Fi Only" else "Wi-Fi & Mobile Data", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = { viewModel.setDriveNetworkType(if (state.driveNetworkType == "WIFI") "ANY" else "WIFI") }) {
                        Text("CHANGE")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Backup Button + Real Time Progress
                val manualWorkInfos by workManager.getWorkInfosForUniqueWorkFlow("ManualDriveBackup").collectAsState(initial = emptyList())
                val manualWorkInfo = manualWorkInfos.firstOrNull()
                val isRunning = manualWorkInfo?.state == WorkInfo.State.RUNNING || manualWorkInfo?.state == WorkInfo.State.ENQUEUED
                
                // Read detailed progress
                val scannedProg = manualWorkInfo?.progress?.getInt("SCANNED", 0) ?: 0
                val uploadedProg = manualWorkInfo?.progress?.getInt("UPLOADED", 0) ?: 0
                val totalProg = manualWorkInfo?.progress?.getInt("TOTAL", 0) ?: 0
                
                // Read final result safely using side-effects
                val isSucceeded = manualWorkInfo?.state == WorkInfo.State.SUCCEEDED
                val finalUploadedCount = if (isSucceeded) manualWorkInfo?.outputData?.getInt("UPLOADED", -1) ?: -1 else -1

                var hasShownCompletionToast by remember(manualWorkInfo?.id) { mutableStateOf(false) }

                androidx.compose.runtime.LaunchedEffect(isSucceeded, manualWorkInfo?.id) {
                    if (isSucceeded && !hasShownCompletionToast) {
                        hasShownCompletionToast = true
                        if (finalUploadedCount == 0) {
                            android.widget.Toast.makeText(context, "Already safely backed up! No new files.", android.widget.Toast.LENGTH_LONG).show()
                        } else if (finalUploadedCount > 0) {
                            android.widget.Toast.makeText(context, "Successfully backed up $finalUploadedCount new files!", android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val workReq = OneTimeWorkRequestBuilder<PdfDriveBackupWorker>().build()
                            workManager.enqueueUniqueWork("ManualDriveBackup", androidx.work.ExistingWorkPolicy.REPLACE, workReq)
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        enabled = !isRunning
                    ) {
                        if (isRunning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp), 
                                color = MaterialTheme.colorScheme.onPrimary, 
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            if (uploadedProg > 0) {
                                Text("Uploading missing: $uploadedProg")
                            } else if (scannedProg > 0) {
                                Text("Scanning diffs... $scannedProg / $totalProg")
                            } else {
                                Text("Starting Engine...")
                            }
                        } else {
                            val lastBackupStr = if (state.lastBackupTime > 0) java.text.SimpleDateFormat("dd MMM, hh:mm a", java.util.Locale.US).format(java.util.Date(state.lastBackupTime)) else "Never"
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Back up now (Last: $lastBackupStr)")
                        }
                    }
                    
                    if (isRunning) {
                        IconButton(
                            onClick = {
                                workManager.cancelUniqueWork("ManualDriveBackup")
                            },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Abort Backup", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                 Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun SettingsTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        modifier = Modifier.clickable { onClick() },
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) }
    )
}
