package com.dollyplastic.invoiceapp.ui.screens.processing.components

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Environment
import android.webkit.CookieManager
import androidx.core.content.ContextCompat
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.data.models.InvoiceStorageRef
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import java.io.File


class PortalDownloadHandler(
    private val storageRef: InvoiceStorageRef,
    private val context: Context, // MUST be applicationContext
    private val invoiceNumber: String,
    private val onFileReady: (File) -> Unit
) {
    private val TAG = "InvoiceWorkflow"
    private var downloadId: Long = -1L

    fun startDownload(
        url: String,
        userAgent: String,
        contentDisposition: String,
        mimeType: String
    ) {
        // Prevent parallel downloads per invoice
        if (downloadId != -1L) return

        val request = DownloadManager.Request(Uri.parse(url))

        CookieManager.getInstance().getCookie(url)?.let {
            request.addRequestHeader("Cookie", it)
        }
        request.addRequestHeader("User-Agent", userAgent)

        android.util.Log.d(TAG, "[Download] Starting download for URL: $url")

        val fileName = android.webkit.URLUtil.guessFileName(url, contentDisposition, mimeType)

        // 🔒 App-specific, deterministic destination via InvoiceStorage
        val folder = InvoiceStorage.getTemporaryDirectory(
            firmIdentifier = storageRef.firmName,
            invoiceNumber = storageRef.invoiceNumber
        )
        
        // Ensure parent exists
        if (!folder.exists()) folder.mkdirs()

        // Downloads/Invoice_App/Temporary_Files/Firm_Inv/Filename
        val subPath = "Invoice_App/Temporary_Files/${folder.name}"
        
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            "$subPath/$fileName"
        )

        request.setNotificationVisibility(
            DownloadManager.Request.VISIBILITY_VISIBLE
        )

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = dm.enqueue(request)

        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)

        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )

    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(ctx: Context, intent: Intent) {
            val completedId =
                intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (completedId != downloadId) return

            val dm = ctx.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val query = DownloadManager.Query().setFilterById(downloadId)
            val cursor = dm.query(query)

            if (cursor.moveToFirst()) {
                val status =
                    cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    val localUri =
                        cursor.getString(
                            cursor.getColumnIndexOrThrow(
                                DownloadManager.COLUMN_LOCAL_URI
                            )
                        )
                    
                    val uri = Uri.parse(localUri)
                    val path = uri.path ?: ""
                    val downloadedFile = File(path)
                    
                    android.util.Log.d(TAG, "[Download] Download Complete. URI: $localUri, Path: ${downloadedFile.absolutePath}")
                    
                    if (downloadedFile.exists()) {
                         // RENAME to Portal_Source_Document.{ext}
                         val extension = downloadedFile.extension
                         val parent = downloadedFile.parentFile
                         val standardName = InvoiceStorage.TempFileType.PORTAL_DOWNLOAD.fileName + "." + extension
                         val destFile = File(parent, standardName)
                         
                         if (downloadedFile.renameTo(destFile)) {
                             android.util.Log.d(TAG, "[Download] Renamed to: ${destFile.name}")
                             onFileReady(destFile)
                         } else {
                             android.util.Log.e(TAG, "[Download] Rename Failed. Using original.")
                             onFileReady(downloadedFile)
                         }
                    } else {
                         android.util.Log.e(TAG, "[Download] File does not exist at path: ${downloadedFile.absolutePath}")
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
                    android.util.Log.e(TAG, "[Download] Download Failed. Reason Code: $reason")
                }
            }

            cursor.close()

            try {
                ctx.unregisterReceiver(this)
            } catch (_: Exception) {}
        }
    }
}


fun cleanupInvoiceTemp(context: Context, invoice: Invoice) {
    InvoiceStorage.deleteTempDirectory(
        firm = invoice.firm,
        invoiceNumber = invoice.invoiceNumber
    )
}
