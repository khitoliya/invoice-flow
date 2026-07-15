package com.dollyplastic.invoiceapp.pdf

import android.content.Context
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.properties.AreaBreakType
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InvoicePdfMerger @Inject constructor() {

    enum class PageType(val label: String) {
        ORIGINAL("ORIGINAL FOR RECIPIENT"),
        DUPLICATE("DUPLICATE FOR TRANSPORTER"),
        TRIPLICATE("TRIPLICATE FOR SUPPLIER"),
        EWAY_BILL("E-WAY BILL")
    }

    fun generateCombinedPdf(
        context: Context,
        invoices: List<Invoice>,
        pageTypes: Set<PageType>,
        destFile: File
    ): Result<File> {
        return try {
            val writer = PdfWriter(destFile)
            val pdf = PdfDocument(writer)
            val document = Document(pdf, PageSize.A4)
            document.setMargins(10f, 20f, 10f, 20f)

            invoices.forEachIndexed { index, invoice ->
                val sortedPages = pageTypes.sortedBy { it.ordinal } // Ensure Org -> Dup -> Trip order
                
                sortedPages.forEachIndexed { pageIndex, type ->
                    if (type == PageType.EWAY_BILL) {
                        if (invoice.generateEWayBill && invoice.eWayBillDetails != null) {
                             InvoicePdfGenerator.drawEWayBillPage(document, invoice)
                             // Close page unless it's the very last page of the very last invoice
                             if (shouldAddPageBreak(index, invoices.size, pageIndex, sortedPages.size)) {
                                 document.add(com.itextpdf.layout.element.AreaBreak(AreaBreakType.NEXT_PAGE))
                             }
                        }
                    } else {
                        InvoicePdfGenerator.drawInvoicePage(document, invoice, type.label, context)
                         if (shouldAddPageBreak(index, invoices.size, pageIndex, sortedPages.size)) {
                             document.add(com.itextpdf.layout.element.AreaBreak(AreaBreakType.NEXT_PAGE))
                         }
                    }
                }
            }

            document.close()
            Result.success(destFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // Helper to determine if we need a page break
    // We need a break if:
    // 1. There are more pages for THIS invoice
    // 2. OR There are more invoices to process
    private fun shouldAddPageBreak(
        currentInvoiceIndex: Int, 
        totalInvoices: Int, 
        currentPageIndex: Int, 
        totalPagesForInvoice: Int
    ): Boolean {
        val isLastPageOfInvoice = currentPageIndex == totalPagesForInvoice - 1
        val isLastInvoice = currentInvoiceIndex == totalInvoices - 1
        
        if (!isLastPageOfInvoice) return true
        if (!isLastInvoice) return true
        
        return false
    }
}
