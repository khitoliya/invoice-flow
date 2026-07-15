package com.dollyplastic.invoiceapp.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.dollyplastic.invoiceapp.data.drive.GoogleDriveRepository
import com.dollyplastic.invoiceapp.data.settings.SettingsRepository
import com.dollyplastic.invoiceapp.data.repository.InvoiceStorage
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class PdfDriveBackupWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val driveRepository: GoogleDriveRepository,
    private val settingsRepository: SettingsRepository,
    private val invoiceRepository: InvoiceRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val TAG = "PdfDriveBackupWorker"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Starting PDF Drive Backup Worker...")

            // 1. Initial Checks
            if (!settingsRepository.isDriveBackupEnabled()) {
                Log.d(TAG, "Backup is disabled in settings. Aborting.")
                return@withContext Result.success()
            }

            if (!driveRepository.initializeFromSignedInAccount()) {
                Log.w(TAG, "Failed to initialize Drive API. User not signed in properly.")
                return@withContext Result.failure()
            }

            val folderId = settingsRepository.extractDriveFolderId()

            // 2. Scan Local Storage
            val baseInvoiceDir = InvoiceStorage.getInvoiceDataDirectory()
            if (!baseInvoiceDir.exists() || !baseInvoiceDir.isDirectory) {
                Log.d(TAG, "No invoice data directory found.")
                return@withContext Result.success()
            }

            // We iterate iteratively using a new recursive mirroring logic
            val totalPdfs = baseInvoiceDir.walkTopDown().count { it.isFile && it.extension.equals("pdf", ignoreCase = true) }
            
            if (totalPdfs > 0) {
                setProgress(workDataOf("CURRENT" to 0, "TOTAL" to totalPdfs))
            } else {
                Log.d(TAG, "No PDFs found to backup.")
                return@withContext Result.success()
            }
            
            Log.d(TAG, "Proceeding to mirror $totalPdfs PDFs sequentially preserving folder structure.")
            
            val counter = intArrayOf(0, 0, 0) // [0] = Scanned, [1] = GenuineUploads, [2] = UploadFails

            // Start recursive tree mapping
            syncDirectoryRecursive(baseInvoiceDir, folderId, totalPdfs, counter)

            Log.d(TAG, "Backup Complete. Total Scanned: ${counter[0]}, Genuine Uploads: ${counter[1]}, Fails: ${counter[2]}")
            settingsRepository.setLastBackupTimestamp(System.currentTimeMillis())

            return@withContext Result.success(workDataOf("UPLOADED" to counter[1]))
        } catch (e: Exception) {
            Log.e(TAG, "Critical Worker Failure: ${e.message}", e)
            return@withContext Result.retry()
        }
    }

    private suspend fun syncDirectoryRecursive(
        localDir: java.io.File, 
        driveParentId: String?, 
        totalPdfs: Int, 
        counter: IntArray
    ) {
        if (!localDir.isDirectory || isStopped) return
        
        // Ensure this folder exists in Drive cleanly
        val thisDriveFolderId = driveRepository.getOrCreateFolder(localDir.name, driveParentId).getOrElse { 
            Log.e(TAG, "Failed mirroring remote folder ${localDir.name}: ${it.message}")
            return // abort processing this branch cleanly without failing whole job
        }
        
        // Fast differential sync check
        val remoteFiles = driveRepository.getFilesInFolder(thisDriveFolderId).getOrDefault(emptySet())
        
        // Sorting directories first ensures hierarchy is built optimally
        val children = localDir.listFiles()?.sortedBy { if (it.isDirectory) 0 else 1 } ?: emptyList()
        
        for (child in children) {
            if (isStopped) return
            
            if (child.isDirectory) {
                syncDirectoryRecursive(child, thisDriveFolderId, totalPdfs, counter)
            } else if (child.isFile && child.extension.equals("pdf", ignoreCase = true)) {
                counter[0]++ // Increment scanned counter
                
                if (!remoteFiles.contains(child.name)) {
                    val result = driveRepository.uploadPdf(child, thisDriveFolderId)
                    if (result.isSuccess) counter[1]++ else counter[2]++
                }
                
                // Emit real-time progress update
                setProgress(workDataOf("SCANNED" to counter[0], "TOTAL" to totalPdfs, "UPLOADED" to counter[1]))
            }
        }
    }
}
