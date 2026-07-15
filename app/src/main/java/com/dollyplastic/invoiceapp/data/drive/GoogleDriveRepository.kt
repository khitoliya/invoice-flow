package com.dollyplastic.invoiceapp.data.drive

import android.content.Context
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDriveRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val TAG = "GoogleDriveRepo"
    
    // The Drive Client instance
    private var driveService: Drive? = null

    /**
     * Initializes the Drive service using an actively signed-in account.
     */
    fun initializeFromSignedInAccount(): Boolean {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE))) {
            setupDriveService(account)
            true
        } else {
            false
        }
    }

    private fun setupDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        driveService = Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
        .setApplicationName("InvoiceApp Automated Backup")
        .build()
        
        Log.d(TAG, "Drive Service initialized successfully.")
    }

    /**
     * Safely uploads a local PDF file to the designated Google Drive Folder.
     * Uses Dispatchers.IO internally.
     */
    suspend fun uploadPdf(file: File, folderId: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: throw IllegalStateException("Drive API not initialized. User not signed in or scopes missing.")

            // 1. Build File Metadata
            val fileMetadata = com.google.api.services.drive.model.File().apply {
                name = file.name
                if (!folderId.isNullOrBlank()) {
                    parents = listOf(folderId)
                }
            }

            // 2. Wrap local file content
            val mediaContent = FileContent("application/pdf", file)

            // 3. Execute Upload
            Log.d(TAG, "Starting upload for ${file.name} to folder: $folderId")
            val uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id, parents")
                .execute()

            Log.d(TAG, "Upload successful: ID = ${uploadedFile.id}")
            Result.success(uploadedFile.id)

        } catch (e: Exception) {
            Log.e(TAG, "Upload Failed for ${file.name}: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Recursively checks if a folder exists in Google Drive by its name and parent, returning its ID.
     * If not found, natively creates a new Drive folder.
     */
    suspend fun getOrCreateFolder(name: String, parentId: String?): Result<String> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: throw IllegalStateException("Drive API not initialized.")
            val safeName = name.replace("'", "\\'") // Minor sanitization
            
            val query = StringBuilder("mimeType='application/vnd.google-apps.folder' and name='$safeName' and trashed=false")
            if (parentId != null) query.append(" and '$parentId' in parents")
            
            val list = service.files().list().setQ(query.toString()).setSpaces("drive").setFields("files(id, name)").execute()
            if (list.files.isNotEmpty()) {
                return@withContext Result.success(list.files.first().id)
            }
            
            val folderMetadata = com.google.api.services.drive.model.File().apply {
                this.name = name
                this.mimeType = "application/vnd.google-apps.folder"
                if (parentId != null) this.parents = listOf(parentId)
            }
            val folder = service.files().create(folderMetadata).setFields("id").execute()
            Result.success(folder.id)
        } catch (e: Exception) {
            Log.e(TAG, "Failed creating subfolder $name: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Queries Google Drive to retrieve all active filenames inside a specific folder ID.
     * Used for ultra-fast differential sync logic.
     */
    suspend fun getFilesInFolder(folderId: String?): Result<Set<String>> = withContext(Dispatchers.IO) {
        try {
            val service = driveService ?: throw IllegalStateException("Drive API not initialized.")
            val query = java.lang.StringBuilder("mimeType!='application/vnd.google-apps.folder' and trashed=false")
            if (folderId != null) query.append(" and '$folderId' in parents")
            
            val filenames = mutableSetOf<String>()
            var pageToken: String? = null
            do {
                val result = service.files().list()
                    .setQ(query.toString())
                    .setSpaces("drive")
                    .setFields("nextPageToken, files(id, name)")
                    .setPageToken(pageToken)
                    .execute()
                filenames.addAll(result.files.map { it.name })
                pageToken = result.nextPageToken
            } while (pageToken != null)
            
            Result.success(filenames)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
