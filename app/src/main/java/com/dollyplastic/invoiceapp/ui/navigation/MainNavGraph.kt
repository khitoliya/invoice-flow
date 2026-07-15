package com.dollyplastic.invoiceapp.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.ui.screens.home.HomeScreen

import com.dollyplastic.invoiceapp.ui.screens.masters.MastersScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.firm.FirmFormScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.firm.FirmListScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.item.ItemFormScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.item.ItemListScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartyFormScreen
import com.dollyplastic.invoiceapp.ui.screens.masters.party.PartyListScreen
import com.dollyplastic.invoiceapp.ui.screens.processing.PortalScreen
import com.dollyplastic.invoiceapp.ui.screens.settings.SettingsScreen
import com.dollyplastic.invoiceapp.ui.screens.settings.credentials.CredentialsScreen
import com.dollyplastic.invoiceapp.ui.screens.settings.recyclebin.RecycleBinScreen
import com.dollyplastic.invoiceapp.ui.screens.settings.config.GstConfigScreen
import com.dollyplastic.invoiceapp.ui.screens.settings.config.StateConfigScreen
import com.dollyplastic.invoiceapp.ui.screens.processing.InvoiceProcessingViewModel
import com.dollyplastic.invoiceapp.ui.screens.processing.InvoiceProcessingScreen
import com.dollyplastic.invoiceapp.pdf.InvoicePdfGenerator
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.credentials.Credential
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.dollyplastic.invoiceapp.ui.screens.import_workflow.ImportSelectionScreen
import com.dollyplastic.invoiceapp.ui.screens.import_workflow.VerificationScreen
import android.util.Log
import android.widget.Toast
import android.content.Intent
import androidx.core.content.FileProvider

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.*
import com.dollyplastic.invoiceapp.ui.screens.invoice.InvoiceScreen.InvoiceCreationScreen

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
fun NavGraphBuilder.mainGraph(
    navController: NavHostController
) {
    navigation(
        startDestination = Route.Home.route,
        route = Route.Main.route
    ) {


        composable(Route.Home.route) { 
            HomeScreen(
                onNavigateToCreate = { /* Unused on Dashboard */ },
                onNavigateToProcessing = { /* Unused on Dashboard */ },
                onNavigateToPortal = { _, _, _, _, _, _ -> /* Unused on Dashboard */ },
                onNavigateToSettings = { navController.navigate(Route.Settings.route) }
            ) 
        }
        
        composable(Route.Invoices.route) {
            com.dollyplastic.invoiceapp.ui.screens.invoice.list.InvoiceListScreen(
                onNavigateToCreate = { navController.navigate(Route.InvoiceCreation.create()) },
                onNavigateToProcessing = { id -> navController.navigate(Route.InvoiceProcessing.create(id, autoStart = false)) },
                onNavigateToPortal = { invoiceId, url, storageRef, credentialsJson, isCancellation, mode ->
                    navController.navigate(
                        Route.Portal.create(invoiceId, url, storageRef, credentialsJson, isCancellation, mode)
                    )
                }
            )
        }
        
        composable(Route.Purchases.route) {
            com.dollyplastic.invoiceapp.ui.screens.purchase.PurchaseListScreen()
        }

        composable(
            route = Route.InvoiceCreation.route,
            arguments = listOf(
                navArgument("invoiceId") {
                    nullable = true
                    defaultValue = null
                }
            )
        ) { 
            InvoiceCreationScreen(
                onNavigateToProcessing = { invoiceId, autoStart -> navController.navigate(Route.InvoiceProcessing.create(invoiceId, autoStart)) },
                navController = navController
            )
        }
        composable(Route.Masters.route) { MastersScreen(navController) }
        composable(Route.Settings.route) { 
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCredentials = { navController.navigate(Route.Credentials.route) },
                onNavigateToRecycleBin = { navController.navigate(Route.RecycleBin.route) },
                onNavigateToGstConfig = { navController.navigate(Route.GstConfig.route) },
                onNavigateToStateConfig = { navController.navigate(Route.StateConfig.route) }
            ) 
        }
        composable(Route.Credentials.route) {
             CredentialsScreen(
                 onNavigateBack = { navController.popBackStack() }
             )
        }
        composable(Route.RecycleBin.route) {
             RecycleBinScreen(
                 onNavigateBack = { navController.popBackStack() }
             )
        }
        composable(Route.GstConfig.route) {
             GstConfigScreen(
                 onNavigateBack = { navController.popBackStack() }
             )
        }
        composable(Route.StateConfig.route) {
             StateConfigScreen(
                 onNavigateBack = { navController.popBackStack() }
             )
        }
        //------------------Masters Sub Graph------------------//
        composable(Route.FirmList.route) {
            FirmListScreen(navController)
        }

        composable(
            route = Route.FirmForm.route,
            arguments = listOf(
                navArgument("firmId") {
                    nullable = true
                    defaultValue = null
                }
            )
        ) {
            val firmId = it.arguments?.getString("firmId")
            FirmFormScreen(navController, firmId)
        }
    }

    composable(Route.PartyList.route) {
        PartyListScreen(navController)
    }

    composable(
        route = Route.PartyForm.route,
        arguments = listOf(
            navArgument("partyId") {
                nullable = true
                defaultValue = null
            }
        )
    ) {
        val partyId = it.arguments?.getString("partyId")
        PartyFormScreen(navController, partyId)
    }
    composable(Route.ItemList.route) {
        ItemListScreen(navController)
    }

    composable(
        route = Route.ItemForm.route,
        arguments = listOf(
            navArgument("itemId") {
                nullable = true
                defaultValue = null
            }
        )
    ) {
        val itemId = it.arguments?.getString("itemId")
        ItemFormScreen(navController, itemId)
    }


    composable(
        route = Route.InvoiceProcessing.route,
        arguments = listOf(
            navArgument("invoiceId") { type = androidx.navigation.NavType.StringType },
            navArgument("autoStart") {
                type = androidx.navigation.NavType.BoolType
                defaultValue = false
            }
        )
    ) {
        val invoiceId = it.arguments?.getString("invoiceId") ?: return@composable
        val autoStart = it.arguments?.getBoolean("autoStart") ?: false

        // Get ViewModel scoped to THIS NavBackStackEntry explicitly
        val viewModel = androidx.hilt.navigation.compose.hiltViewModel<InvoiceProcessingViewModel>(it)
        
        // Listen to "download_result" specifically on THIS entry's SavedStateHandle
        val savedStateHandle = it.savedStateHandle
        val liveData = savedStateHandle.getLiveData<String>("download_result")

        androidx.compose.runtime.DisposableEffect(liveData) {
            val observer = androidx.lifecycle.Observer<String?> { path ->
                if (path != null) {
                    Log.d("InvoiceWorkflow", "[NavGraph] Result Observed in Graph: $path")
                    viewModel.onResultFileDownloaded(java.io.File(path))
                    // Clear to prevent re-trigger
                    savedStateHandle["download_result"] = null
                }
            }
            liveData.observeForever(observer)
            onDispose { liveData.removeObserver(observer) }
        }

        InvoiceProcessingScreen(
            invoiceId = invoiceId,
            autoStart = autoStart,
            viewModel = viewModel, // Pass the explicit instance
            onNavigateBack = { navController.popBackStack() },
            onNavigateToPortal = { url, storageRef, credentialsJson, isCancellation, mode ->
                navController.navigate(
                    Route.Portal.create(invoiceId, url, storageRef, credentialsJson, isCancellation, mode)
                )
             },
             onViewPdf = { invoice: Invoice ->
                 val context = navController.context
                 try {
                     val file = InvoicePdfGenerator.generateForAndroid(context, invoice)
                     val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                     val intent = Intent(Intent.ACTION_VIEW).apply {
                         setDataAndType(uri, "application/pdf")
                         flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                     }
                     context.startActivity(intent)
                 } catch (e: Exception) {
                     Log.e("InvoiceWorkflow", "Failed to open PDF", e)
                     Toast.makeText(context, "Failed to open PDF", Toast.LENGTH_SHORT).show()
                 }
             },
             onSharePdf = { file, contact ->
                val context = navController.context
                try {
                    val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        
                        if (contact != null) {
                            // WA Direct Share
                            // Format: https://api.whatsapp.com/send?phone=... is for Links.
                            // For sending FILES, we set package + jid.
                            // Unofficial but widely used method for direct chat targeting.
                            // "jid" extra: "919999999999@s.whatsapp.net"
                            
                            val number = contact.second
                            // Ensure country code. Assuming India (91) if missing, but better to just pass what we have if it starts with +.
                            // Clean number already done in SettingsViewModel (digits only).
                            
                            // Safe check: if number is just 10 digits, prepend 91?
                            val finalNumber = if (number.length == 10) "91$number" else number
                            
                            setPackage("com.whatsapp")
                            putExtra("jid", "$finalNumber@s.whatsapp.net")
                        }
                    }
                    
                    // Try launching
                    try {
                        context.startActivity(intent)
                    } catch (e: android.content.ActivityNotFoundException) {
                        // Fallback: Generic Share or WA Business
                        if (contact != null) {
                            // Retry without package restriction or try WA Business
                            Toast.makeText(context, "WhatsApp not installed, opening options...", Toast.LENGTH_SHORT).show()
                            val fallback = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, uri)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(fallback, "Share Invoice"))
                        } else {
                            // Should have worked for generic share, but safeguard
                             context.startActivity(Intent.createChooser(intent, "Share Invoice"))
                        }
                    }
                } catch (e: Exception) {
                    Log.e("InvoiceWorkflow", "Failed to share PDF", e)
                    Toast.makeText(context, "Failed to share PDF", Toast.LENGTH_SHORT).show()
                }
             },
             onNavigateToEdit = { id ->
                 navController.navigate(Route.InvoiceCreation.create(id))
             }
        )
    }

    composable(
        route = Route.Portal.route,
        arguments = listOf(
            navArgument("invoiceId") { type = NavType.StringType },
            navArgument("url") { type = NavType.StringType },
            navArgument("storageRef") { type = NavType.StringType },
            navArgument("credentials") { type = NavType.StringType },
            navArgument("isCancellation") { 
                type = NavType.BoolType
                defaultValue = false
            },
            navArgument("mode") { 
                type = NavType.StringType
                defaultValue = "EWAY"
            }
        )
    ) {
        val invoiceId = it.arguments!!.getString("invoiceId")!!
        val url = it.arguments!!.getString("url")!!
        val rawRef = it.arguments!!.getString("storageRef")!!
        val rawCreds = it.arguments!!.getString("credentials") ?: "[]"
        val isCancellation = it.arguments!!.getBoolean("isCancellation")
        val mode = it.arguments!!.getString("mode") ?: "EWAY"

        val storageRef = InvoiceStorageRef.decode(rawRef)
        
        // Deserialize
        val type = object : TypeToken<List<Credential>>() {}.type
        val credentials = try {
            Gson().fromJson<List<Credential>>(rawCreds, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        PortalScreen(
            invoiceId = invoiceId,
            url = url,
            storageRef = storageRef,
            credentials = credentials,
            isCancellationFlow = isCancellation,
            portalMode = mode, // Pass to screen
            onClose = { navController.popBackStack() },
            onDownloadSuccess = { file ->
                // Pass Result Back
                val prev = navController.previousBackStackEntry
                Log.d("InvoiceWorkflow", "[NavGraph] onDownloadSuccess. Previous Entry: ${prev?.destination?.route}, File: ${file.absolutePath}")
                
                prev?.savedStateHandle
                    ?.set("download_result", file.absolutePath)
                
                // Close Portal
                navController.popBackStack()
            }
        )
    }

    /* ---------------- IMPORT SUB-GRAPH ---------------- */
    composable(Route.ImportSelection.route) {
        ImportSelectionScreen(
            onNavigateToVerification = { uri ->
                navController.navigate(Route.ImportVerification.create(uri))
            }
        )
    }

    composable(
        route = Route.ImportVerification.route,
        arguments = listOf(
            navArgument("uri") { type = NavType.StringType }
        )
    ) {
        val uri = it.arguments?.getString("uri") ?: return@composable

        VerificationScreen(
            uri = uri,
            onNavigateBack = { navController.popBackStack() }
        )
    }

}

