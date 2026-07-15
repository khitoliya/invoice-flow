package com.dollyplastic.invoiceapp.ui.components

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun PdfViewer(
    pdfPath: String,
    modifier: Modifier = Modifier
) {
    var bitmaps by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    val density = LocalDensity.current 

    LaunchedEffect(pdfPath) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(pdfPath)
                val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = PdfRenderer(descriptor)
                
                val pages = mutableListOf<Bitmap>()
                // Limit to first 3 pages for performance in split view
                val pageCount = renderer.pageCount.coerceAtMost(3)
                
                for (i in 0 until pageCount) {
                    val page = renderer.openPage(i)
                    // Render high quality
                    val width = 1080 
                    val height = (width.toFloat() / page.width * page.height).toInt()
                    
                    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    pages.add(bitmap)
                    page.close()
                }
                renderer.close()
                descriptor.close()
                bitmaps = pages
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LazyColumn(
        modifier = modifier.background(Color.Gray),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(bitmaps.size) { index ->
            Image(
                bitmap = bitmaps[index].asImageBitmap(),
                contentDescription = "Page ${index + 1}",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth().background(Color.White)
            )
        }
    }
}
