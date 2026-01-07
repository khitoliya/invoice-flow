package com.dollyplastic.invoiceapp


import com.dollyplastic.invoiceapp.data.models.* // Import your models
import com.dollyplastic.invoiceapp.pdf.InvoicePdfGenerator // Import your generator
import com.dollyplastic.invoiceapp.pdf.InvoiceTestFactory
// Import your test data factory
import org.junit.Test
import java.io.File

class PdfPreview {

    @Test
    fun `Generate PDF Preview on Desktop`() {
        // 1. Create Mock Data
        val invoice = InvoiceTestFactory.createFullInvoice() // Or createCashSaleInvoice()

        // 2. Define a path on your LAPTOP (Not the phone)
        // Change "YourUserName" to your actual PC username
        // Mac/Linux: "/Users/yourname/Desktop/test_invoice.pdf"
        // Windows: "C:\\Users\\YourName\\Desktop\\test_invoice.pdf"
        val desktopPath = "/Users/rohitkumar/Desktop/test_invoice.pdf"
        val file = File(desktopPath)

        // 3. Run the Generator
        println("Generating PDF at: ${file.absolutePath}")
        InvoicePdfGenerator.drawPdf(file, invoice)
        println("✅ Done! Open the file to check.")
    }
}