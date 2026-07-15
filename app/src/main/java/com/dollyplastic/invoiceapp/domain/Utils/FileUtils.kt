package com.dollyplastic.invoiceapp.domain.Utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {

    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            
            if (inputStream == null) return null
            
            // Create a temp file
            val tempFile = File.createTempFile("imported_invoice_", ".pdf", context.cacheDir)
            tempFile.deleteOnExit()
            
            val out = FileOutputStream(tempFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            inputStream.close()
            out.flush()
            out.close()
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
