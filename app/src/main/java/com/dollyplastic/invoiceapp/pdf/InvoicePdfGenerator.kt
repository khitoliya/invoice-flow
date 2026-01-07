package com.dollyplastic.invoiceapp.pdf

import android.content.Context
import android.os.Environment
import com.dollyplastic.invoiceapp.data.models.Invoice
import com.dollyplastic.invoiceapp.domain.Utils.NumberToWords
import com.itextpdf.barcodes.BarcodeQRCode
import com.itextpdf.io.font.constants.StandardFonts
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceGray
import com.itextpdf.kernel.font.PdfFontFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.properties.AreaBreakType
import com.itextpdf.layout.properties.HorizontalAlignment
import com.itextpdf.layout.properties.Property
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment
import java.io.File

object InvoicePdfGenerator {

    // 1. Android Entry Point (Keeps your app working)
    fun generateForAndroid(context: Context, invoice: Invoice): File {
        // Android-specific file creation
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (!downloadsDir.exists()) downloadsDir.mkdirs()

        val safeNumber = invoice.invoiceNumber.replace("/", "_")
        val file = File(downloadsDir, "Invoice_$safeNumber.pdf")

        // Call the shared drawing logic
        drawPdf(file, invoice, context)
        return file
    }

    // 2. Shared Drawing Logic (Works on Laptop AND Android)
    // PASS THE FILE OBJECT DIRECTLY
    fun drawPdf(destFile: File, invoice: Invoice, context: Context?=null) {
        val writer = PdfWriter(destFile)
        val pdf = PdfDocument(writer)
        val document = Document(pdf, PageSize.A4)
        document.setMargins(10f, 20f, 10f, 20f)

        val copies = listOf(
            "ORIGINAL FOR RECIPIENT",
            "DUPLICATE FOR TRANSPORTER",
            "TRIPLICATE FOR SUPPLIER"
        )

        for ((index, label) in copies.withIndex()) {

            // 1. Header
            drawTopHeader(document, invoice, label)

            // 2. Main Grid
            party_InvoiceMeta_Section(document, invoice)

            // 3. Item Table
            drawItemTable(document, invoice)

            // 4. Tax Summary
            drawTaxAnalysisTable(document, invoice)

            // 5. Footer
            drawFooterSection(document, invoice, context)

            // --- PAGE BREAK LOGIC ---
            if (index < copies.size - 1) {
                document.add(com.itextpdf.layout.element.AreaBreak(AreaBreakType.NEXT_PAGE))
            }
        }

        document.close()
    }

    // ==========================================
    // TOP HEADER SECTION
    // ==========================================
    private fun drawTopHeader(document: Document, invoice: Invoice, copyLabel: String) {

        // MASTER TABLE: 3 Columns
        // Col 1 (55%): "Tax Invoice"
        // Col 2 (25%): Copy Label
        // Col 3 (20%): e-Invoice + QR
        val headerTable = Table(UnitValue.createPercentArray(floatArrayOf(55f, 25f, 20f)))
        headerTable.useAllAvailableWidth()
        headerTable.setMarginBottom(2f) // Ultra-tight spacing before the main grid

        val hasEInvoice = invoice.eInvoiceDetails != null && invoice.eInvoiceDetails.irn.isNotBlank()

        // --- ROW 1: TITLES ---

        // 1. "Tax Invoice" (Left Aligned)
        headerTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .add(Paragraph("Tax Invoice").setBold().setFontSize(11f)) // Reduced to 11f for compactness
        )

        // 2. Copy Label (Right Aligned)
        headerTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .add(Paragraph("($copyLabel)").setItalic().setFontSize(8f))
        )

        // 3. "e-Invoice" Label (Right Aligned to match QR)
        val eInvLabel = if (hasEInvoice) "e-Invoice" else ""
        headerTable.addCell(
            Cell().setBorder(Border.NO_BORDER)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)
                .setPaddingBottom(0f) // Crucial: Remove padding so it touches QR
                .add(Paragraph(eInvLabel).setBold().setFontSize(9f)).setPaddingRight(20f)
        )


        // --- ROW 2: DATA (IRN + QR) ---

        if (hasEInvoice) {
            // LEFT SIDE: IRN Details (Spans Col 1 & 2)
            // Aligned BOTTOM so it sits flush with the main invoice grid
            val irnCell = Cell(1, 2).setBorder(Border.NO_BORDER)
                .setVerticalAlignment(VerticalAlignment.BOTTOM)

            fun addIrnLine(label: String, value: String) {
                irnCell.add(Paragraph()
                    .add(Text(label).setFontSize(8f).setBold())
                    .add(Text(" : $value").setFontSize(8f))
                    .setFixedLeading(9f) // Tight line spacing
                )
            }
            addIrnLine("IRN", invoice.eInvoiceDetails!!.irn)
            addIrnLine("Ack No.", invoice.eInvoiceDetails.ackNo)
            irnCell.add(Paragraph()
                .add(Text("Ack Date").setFontSize(8f).setBold())
                .add(Text(" : ${invoice.eInvoiceDetails.ackDate}").setFontSize(8f))
                .setFixedLeading(9f) .setPaddingBottom(2f)// Tight line spacing
            )

            headerTable.addCell(irnCell)


            // RIGHT SIDE: QR Code (Occupies Col 3)
            val qrCell = Cell().setBorder(Border.NO_BORDER).setPadding(0f)
                .setTextAlignment(TextAlignment.RIGHT)
                .setVerticalAlignment(VerticalAlignment.TOP) // 🔥 FIX: Pins QR to TOP (Closes Label Gap)

            try {
                val qrString = if (invoice.eInvoiceDetails.signedQrCode.isNotBlank())
                    invoice.eInvoiceDetails.signedQrCode
                else invoice.eInvoiceDetails.irn

                val qrCode = BarcodeQRCode(qrString)
                val qrImage = Image(qrCode.createFormXObject(document.pdfDocument))

                // 🔥 SIZE ADJUSTMENT:
                // Reduced to 72f (approx 1 inch). This shrinks the whole header height.
                qrImage.setWidth(72f).setHeight(72f)

                // Force image to right edge
                qrImage.setHorizontalAlignment(HorizontalAlignment.RIGHT)

                qrCell.add(qrImage)
            } catch (e: Exception) {
                qrCell.add(Paragraph("[QR]").setFontSize(8f))
            }

            headerTable.addCell(qrCell)

        } else {
            // If no e-invoice, simply close the row with no height
            headerTable.addCell(Cell(1, 3).setBorder(Border.NO_BORDER))
        }

        document.add(headerTable)
    }

    // ==========================================
    // SECTION 1: Party & Invoice Meta Section
    // ==========================================
    private fun party_InvoiceMeta_Section(document: Document, invoice: Invoice){
        // Two columns: 50% Left (Parties) | 50% Right (Details)
        val mainTable = Table(UnitValue.createPercentArray(floatArrayOf(60f, 40f)))
        mainTable.useAllAvailableWidth()

        // ==========================================
        // LEFT SIDE: PARTIES (Seller, Consignee, Buyer)
        // ==========================================
        val leftBlock = Table(1).useAllAvailableWidth()

        // A. SELLER (Manually prepare data)

        leftBlock.addCell(
            drawPartyCell(
                title = null, // No title for seller
                name = invoice.firm.tradeName,
                gstin = invoice.firm.gstin,
                stateName = invoice.firm.state,
                stateCode = invoice.firm.stateCode,
                fixedHeight = 70f, // Taller for seller
                isBoldName = true,
                addressLine1 = invoice.firm.addressLine1 ,
                addressLine2 = invoice.firm.addressLine2 ,
                city = invoice.firm.city ,
                pincode = invoice.firm.pincode
            )
        )

        // B. CONSIGNEE (Ship To)
        val isCash = invoice.isCashSale
        val consigneeName = if (isCash) "CASH SALE" else (invoice.shipToParty?.tradeName ?: "")

        leftBlock.addCell(
            drawPartyCell(
                title = "Consignee (Ship to)",
                name = consigneeName,
                gstin = invoice.shipToParty?.gstin ?: "",
                stateName = invoice.shipToParty?.state ?: "",
                stateCode = invoice.shipToParty?.stateCode ?: "",
                fixedHeight = 70f,
                isBoldName = true,
                addressLine1 = invoice.shipToParty?.addressLine1 ?: "",
                addressLine2 = invoice.shipToParty?.addressLine2 ?: "",
                city = invoice.shipToParty?.city ?: "",
                pincode = invoice.shipToParty?.pincode ?: ""
            )
        )

        // C. BUYER (Bill To) - HANDLE CASH SALE HERE
        // If Cash Sale -> Name is "CASH SALE", Address is empty, GSTIN is empty.
        val buyerName = if (isCash) "CASH SALE" else (invoice.billToParty?.tradeName ?: "")
        val buyerGstin = if (isCash) "" else (invoice.billToParty?.gstin ?: "")
        val buyerState = if (isCash) "" else (invoice.billToParty?.state ?: "")
        val buyerCode = if (isCash) "" else (invoice.billToParty?.stateCode ?: "")

        leftBlock.addCell(
            drawPartyCell(
                title = "Buyer (Bill to)",
                name = buyerName,
                gstin = buyerGstin,
                stateName = buyerState,
                stateCode = buyerCode,
                fixedHeight = 70f,
                isBoldName = true,
                addressLine1 = invoice.billToParty?.addressLine1 ?: "",
                addressLine2 = invoice.billToParty?.addressLine2 ?: "",
                city = invoice.billToParty?.city ?: "",
                pincode = invoice.billToParty?.pincode ?: ""
            )
        )

        // Add Left Block to Main Table
        mainTable.addCell(Cell().add(leftBlock).setPadding(0f).setBorder(Border.NO_BORDER))


        // ==========================================
        // RIGHT SIDE: INVOICE META DATA
        // ==========================================
        val rightBlock = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
        rightBlock.useAllAvailableWidth()
        rightBlock.setProperty(Property.TABLE_LAYOUT, "fixed")  // 🔥 THIS IS THE KEY


        // Row 1: Split Cell (Invoice No | E-Way) + Dated
        // -------- ROW 1 : Invoice No + e-Way Bill (SPECIAL CASE) --------

// Outer cell (single border, no divider inside)
        val invoiceEwayCell = Cell()
            .setBorder(SolidBorder(0.5f))
            .setPadding(2f)
            .setHeight(22f)

// Inner table ONLY for layout (no borders)
        val inner = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
        inner.useAllAvailableWidth()
        inner.setProperty(Property.TABLE_LAYOUT, "fixed")

// LEFT: Invoice No
        val invoicePara = Paragraph()
            .add(Text("Invoice No.\n")
                .setFontSize(7f)

                .setFontColor(ColorConstants.DARK_GRAY))
            .add(
                Text(invoice.invoiceNumber)
                    .setFontSize(7f)          // 🔥 smaller to fit
                    .setBold()
            )

        inner.addCell(
            Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0f)
                .add(invoicePara)
        )

// RIGHT: e-Way Bill No
        val ewayPara = Paragraph()
            .add(Text("e-Way Bill No.\n")
                .setFontSize(7f)
                .setFontColor(ColorConstants.DARK_GRAY))
            .add(Text(invoice.eWayBillDetails?.ewayBillNo ?: "")
                .setFontSize(7f)          // 🔥 smaller
                .setBold())

        inner.addCell(
            Cell()
                .setBorder(Border.NO_BORDER)
                .setPadding(0f)
                .add(ewayPara)
        )

        invoiceEwayCell.add(inner)

// Add to right block
        rightBlock.addCell(invoiceEwayCell)




// Normal dated cell
        rightBlock.addCell(drawMetaCell("Dated", invoice.invoiceDate))


        // Row 2
        rightBlock.addCell(drawMetaCell("Delivery Note", invoice.additionalDetails?.deliveryNoteNo ?: ""))
        rightBlock.addCell(drawMetaCell("Mode/Terms of Payment", invoice.additionalDetails?.paymentMode?.name ?: ""))

        // Row 3
        rightBlock.addCell(drawMetaCell("Reference No. & Date", invoice.additionalDetails?.referenceNo ?: ""))
        rightBlock.addCell(drawMetaCell("Other References", invoice.additionalDetails?.otherReferences ?: ""))

        // Row 4
        rightBlock.addCell(drawMetaCell("Buyer's Order No.", invoice.additionalDetails?.buyerOrderNo ?: ""))
        rightBlock.addCell(drawMetaCell("Dated", invoice.invoiceDate))

        // Row 5
        rightBlock.addCell(drawMetaCell("Dispatch Doc No.", invoice.transportDetails.transporterDocNo ?: ""))
        rightBlock.addCell(drawMetaCell("Delivery Note Date", invoice.additionalDetails?.deliveryNoteDate ?: ""))

        // Row 6
        rightBlock.addCell(drawMetaCell("Dispatched through", invoice.transportDetails.transporterName ?: ""))
        rightBlock.addCell(drawMetaCell("Destination", invoice.transportDetails.portOfDischarge ?: ""))

        // Row 7
        val lrText = if (!invoice.transportDetails.transporterDocNo.isNullOrBlank())
            "${invoice.transportDetails.transporterDocNo} dt. ${invoice.transportDetails.transporterDocDate}" else ""
        rightBlock.addCell(drawMetaCell("Bill of Lading/LR-RR No.", lrText))
        rightBlock.addCell(drawMetaCell("Motor Vehicle No.", invoice.transportDetails.vehicleNumber ?: ""))

        // -------- TERMS OF DELIVERY (SPECIAL BALANCER ROW) --------

        val termsOuterCell = Cell(1, 2)   // span full width
            .setBorder(SolidBorder(0.5f))
            .setPadding(3f)
            .setHeight(43.5f)               // 🔥 FIXED HEIGHT (always)
            .setVerticalAlignment(VerticalAlignment.TOP)

// Label (always present)
        termsOuterCell.add(
            Paragraph("Terms of Delivery")
                .setFontSize(7f)
                .setFontColor(ColorConstants.DARK_GRAY)
                .setFixedLeading(7f)
                .setMarginBottom(3f)
        )

// Value area (must reserve space even if empty)
        val termsText = invoice.additionalDetails?.termsOfDelivery

        if (!termsText.isNullOrBlank()) {
            termsOuterCell.add(
                Paragraph(termsText)
                    .setFontSize(9f)
                    .setBold()
                    .setFixedLeading(10f)
            )
        } else {
            // 🔥 Force visual balance when empty
            termsOuterCell.add(
                Paragraph("")
                    .setHeight(20f)   // reserves space so block does not collapse
            )
        }

        rightBlock.addCell(termsOuterCell)



        // Add Right Block to Main Table
        mainTable.addCell(Cell().add(rightBlock).setPadding(0f).setBorder(Border.NO_BORDER))

        document.add(mainTable)
    }

    // ==========================================
    // SECTION 2: ITEM TABLE
    // ==========================================
    private fun drawItemTable(document: Document, invoice: Invoice) {

        // 1. DEFINE COLUMNS
        // Page Width = 595 - 40 (margins) = 555 pts usable width
        val colWidths = floatArrayOf(3f, 43f, 8f, 10f, 8f, 4f, 12f)
        val table = Table(UnitValue.createPercentArray(colWidths))
        table.useAllAvailableWidth()

        val headerSize = 8f
        val dataSize = 9f

        // --- 2. HEADER ROWS ---
        fun addHeader(text: String, align: TextAlignment = TextAlignment.CENTER) {
            table.addCell(
                Cell().add(Paragraph(text).setBold().setFontSize(headerSize))
                    .setTextAlignment(align)
                    .setBackgroundColor(DeviceGray(0.95f))
                    .setBorder(SolidBorder(0.5f))
                    .setPadding(3f)
            )
        }

        addHeader("SI")
        addHeader("Description of Goods", TextAlignment.LEFT)
        addHeader("HSN/SAC")
        addHeader("Quantity")
        addHeader("Rate")
        addHeader("per")
        addHeader("Amount")


        // --- 3. PREPARE DATA ---
        val taxGroups = invoice.items
            .groupBy { it.item.gstRate }
            .toSortedMap(reverseOrder())

        val supplyStateCode = invoice.shipToParty?.stateCode ?: invoice.billToParty?.stateCode ?: ""
        val isInterState = invoice.firm.stateCode != supplyStateCode

        var totalQty = 0.0
        var totalVal = 0.0
        var usedHeight = 20f
        var serialNumber = 1


        // --- 4. RENDER LOOP ---

        fun addCell(content: Paragraph, align: TextAlignment, borderBottom: Border? = null) {
            val bottomStyle = borderBottom ?: Border.NO_BORDER
            val cell = Cell().add(content)
                .setTextAlignment(align)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(bottomStyle)
                .setBorderLeft(SolidBorder(0.5f))
                .setBorderRight(SolidBorder(0.5f))
                .setPadding(3f)
            table.addCell(cell)
        }

        for ((rate, items) in taxGroups) {

            // A. ITEMS
            for (invItem in items) {
                val item = invItem.item
                totalQty += invItem.quantity
                totalVal += invItem.taxableValue

                val descLines = (item.name.length / 35) + 1
                usedHeight += (12f * descLines) + 5f

                // SI
                addCell(Paragraph((serialNumber++).toString()).setFontSize(dataSize), TextAlignment.CENTER)

                // Description
                addCell(Paragraph(item.name).setFontSize(dataSize).setBold(), TextAlignment.LEFT)

                // HSN (Max Width 42pts)
                addCell(createAutoFitParagraph(item.hsnCode, 42f), TextAlignment.LEFT)

                // Quantity (Max Width 53pts)
                val qtyText = "${"%.2f".format(invItem.quantity)} ${item.unit}"
                addCell(createAutoFitParagraph(qtyText, 53f, isBold = true), TextAlignment.RIGHT)

                // Rate
                addCell(Paragraph("%.2f".format(invItem.rate)).setFontSize(dataSize), TextAlignment.RIGHT)

                // Per
                addCell(Paragraph(item.unit).setFontSize(dataSize), TextAlignment.LEFT)

                // Amount (Max Width 64pts)
                val amtText = "%.2f".format(invItem.taxableValue)
                addCell(createAutoFitParagraph(amtText, 64f, isBold = true), TextAlignment.RIGHT)
            }

            // B. TAX ROWS
            if (rate > 0) {
                val groupTaxAmount = items.sumOf { it.gstAmount }
                totalVal += groupTaxAmount
                usedHeight += if (isInterState) 15f else 30f

                val rateStr = if (rate % 1.0 == 0.0) rate.toInt().toString() else rate.toString()

                fun addTaxLine(label: String, taxAmt: Double) {
                    addCell(Paragraph(""), TextAlignment.CENTER) // SI

                    val labelPara = Paragraph(label)
                        .setFontSize(dataSize).setBold().setItalic()
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setMarginRight(10f)
                    addCell(labelPara, TextAlignment.RIGHT) // Desc

                    addCell(Paragraph(""), TextAlignment.CENTER) // HSN
                    addCell(Paragraph(""), TextAlignment.CENTER) // Qty
                    addCell(Paragraph(""), TextAlignment.CENTER) // Rate
                    addCell(Paragraph(""), TextAlignment.CENTER) // Per

                    // Tax Amount (Auto-Fit)
                    val taxValStr = "%.2f".format(taxAmt)
                    addCell(createAutoFitParagraph(taxValStr, 64f, isBold = true), TextAlignment.RIGHT)
                }

                if (isInterState) {
                    addTaxLine("IGST $rateStr %", groupTaxAmount)
                } else {
                    val halfRateStr = if ((rate/2) % 1.0 == 0.0) (rate/2).toInt().toString() else (rate/2).toString()
                    val halfTax = groupTaxAmount / 2
                    addTaxLine("CGST $halfRateStr %", halfTax)
                    addTaxLine("SGST $halfRateStr %", halfTax)
                }
            }
        }


        // --- 5. FILLER ROW ---
        val targetMinHeight = 180f
        val remainingHeight = targetMinHeight - usedHeight
        val fillerHeight = if (remainingHeight > 0) remainingHeight else 0f

        if (fillerHeight > 0) {
            for (i in 0 until 7) {
                val filler = Cell()
                    .setHeight(fillerHeight)
                    .setBorderTop(Border.NO_BORDER)
                    .setBorderBottom(SolidBorder(0.5f))
                    .setBorderLeft(SolidBorder(0.5f))
                    .setBorderRight(SolidBorder(0.5f))
                table.addCell(filler)
            }
        } else {
            // Closed logic handled by Total Row
        }


        // --- 6. TOTAL ROW ---
        fun addTotalCell(content: Paragraph, align: TextAlignment, colSpan: Int = 1) {
            val cell = Cell(1, colSpan).add(content)
                .setTextAlignment(align)
                .setBorder(SolidBorder(0.5f))
                .setPadding(3f)
            table.addCell(cell)
        }

        addTotalCell(Paragraph("Total").setFontSize(dataSize).setBold(), TextAlignment.RIGHT, colSpan = 3)

        // Total Qty
        val totalQtyStr = "%.2f KGS".format(totalQty)
        addTotalCell(createAutoFitParagraph(totalQtyStr, 53f, isBold = true), TextAlignment.RIGHT)

        addTotalCell(Paragraph(""), TextAlignment.CENTER) // Rate
        addTotalCell(Paragraph(""), TextAlignment.CENTER) // Per

        // Total Amount
        val totalValStr = "Rs. %.2f".format(totalVal)
        addTotalCell(createAutoFitParagraph(totalValStr, 64f, isBold = true), TextAlignment.RIGHT)

        document.add(table)


        // --- 7. AMOUNT IN WORDS (Merged Row Logic) ---
        val amountInWords = NumberToWords.convert(totalVal)

        // Use a single column table to hold the box
        val wordsTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
        wordsTable.useAllAvailableWidth()

        val containerCell = Cell().setBorderLeft(SolidBorder(0.5f)).setBorderRight(SolidBorder(0.5f)).setPadding(4f)

        // Create a Nested Table for the Top Line (Label Left | E.O.E Right)
        val headerLineTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f)))
        headerLineTable.useAllAvailableWidth()

        // Left: Label
        headerLineTable.addCell(
            Cell().setBorder(Border.NO_BORDER).setPadding(0f)
                .setTextAlignment(TextAlignment.LEFT)
                .add(Paragraph("Amount Chargeable (in words)").setFontSize(8f))
        )

        // Right: E. & O.E
        headerLineTable.addCell(
            Cell().setBorder(Border.NO_BORDER).setPadding(0f)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(Paragraph("E. & O.E").setFontSize(8f).setItalic())
        )

        containerCell.add(headerLineTable)

        // Add the Amount Text below (Full Width)
        // Note: 540f gives it maximum room to expand before shrinking
        containerCell.add(createAutoFitParagraph(amountInWords, 540f, maxFontSize = 10f, isBold = true))

        wordsTable.addCell(containerCell)
        document.add(wordsTable)
    }

    // ==========================================
    // SECTION 3: HSN/TAX ANALYSIS TABLE
    // ==========================================
    private fun drawTaxAnalysisTable(document: Document, invoice: Invoice) {

        // 1. PREPARE DATA
        data class HsnSummary(
            val hsn: String,
            val rate: Double,
            var taxable: Double = 0.0,
            var taxAmount: Double = 0.0
        )

        val hsnMap = mutableMapOf<String, HsnSummary>()

        for (item in invoice.items) {
            val key = item.item.hsnCode
            val summary = hsnMap.getOrPut(key) { HsnSummary(key, item.item.gstRate) }
            summary.taxable += item.taxableValue
            summary.taxAmount += item.gstAmount
        }

        val supplyStateCode = invoice.shipToParty?.stateCode ?: invoice.billToParty?.stateCode ?: ""
        val isInterState = invoice.firm.stateCode != supplyStateCode


        // 2. DEFINE COLUMNS
        val table: Table
        if (isInterState) {
            table = Table(UnitValue.createPercentArray(floatArrayOf(35f, 20f, 10f, 20f, 15f)))
        } else {
            table = Table(UnitValue.createPercentArray(floatArrayOf(28f, 17f, 9f, 13f, 9f, 13f, 11f)))
        }

        table.useAllAvailableWidth()
        table.setMarginTop(0f)

        // Compact Settings
        val headerSize = 7f
        val dataSize = 8f
        val tightPadding = 1f


        // --- 3. HEADER ROWS ---
        fun addHeader(text: String, rowSpan: Int = 1, colSpan: Int = 1) {
            table.addCell(Cell(rowSpan, colSpan)
                .add(Paragraph(text)
                    .setFontSize(headerSize)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFixedLeading(headerSize + 1)
                )
                .setBorder(SolidBorder(0.5f))
                .setPadding(tightPadding))
        }

        // Row 1
        addHeader("HSN/SAC", rowSpan = 2)
        addHeader("Taxable\nValue", rowSpan = 2)

        if (isInterState) {
            addHeader("IGST", colSpan = 2)
        } else {
            addHeader("CGST", colSpan = 2)
            addHeader("SGST", colSpan = 2)
        }

        addHeader("Total\nTax Amount", rowSpan = 2)

        // Row 2
        if (isInterState) {
            addHeader("Rate")
            addHeader("Amount")
        } else {
            addHeader("Rate")
            addHeader("Amount")
            addHeader("Rate")
            addHeader("Amount")
        }


        // --- 4. DATA ROWS (Modified Borders) ---
        var totalTaxable = 0.0
        var totalTaxAmt = 0.0

        fun addData(text: String, align: TextAlignment, isBold: Boolean = false) {
            val p = Paragraph(text)
                .setFontSize(dataSize)
                .setFixedLeading(dataSize + 1)

            if (isBold) p.setBold()

            // 🔥 REMOVED HORIZONTAL SEPARATORS
            // We set Top/Bottom to NO_BORDER so rows merge visually
            table.addCell(Cell().add(p)
                .setTextAlignment(align)
                .setBorderTop(Border.NO_BORDER)
                .setBorderBottom(Border.NO_BORDER)
                .setBorderLeft(SolidBorder(0.5f))  // Keep vertical lines
                .setBorderRight(SolidBorder(0.5f)) // Keep vertical lines
                .setPadding(tightPadding))
        }

        for (summary in hsnMap.values) {
            totalTaxable += summary.taxable
            totalTaxAmt += summary.taxAmount

            // HSN
            addData(summary.hsn, TextAlignment.LEFT)
            // Taxable
            addData("%.2f".format(summary.taxable), TextAlignment.RIGHT, isBold = true)

            val rateStr = if (summary.rate % 1.0 == 0.0) summary.rate.toInt().toString() else summary.rate.toString()

            if (isInterState) {
                addData("$rateStr%", TextAlignment.CENTER)
                addData("%.2f".format(summary.taxAmount), TextAlignment.RIGHT)
            } else {
                val halfRate = summary.rate / 2
                val halfRateStr = if (halfRate % 1.0 == 0.0) halfRate.toInt().toString() else halfRate.toString()
                val halfTax = summary.taxAmount / 2

                addData("$halfRateStr%", TextAlignment.CENTER)
                addData("%.2f".format(halfTax), TextAlignment.RIGHT)
                addData("$halfRateStr%", TextAlignment.CENTER)
                addData("%.2f".format(halfTax), TextAlignment.RIGHT)
            }

            // Total
            addData("%.2f".format(summary.taxAmount), TextAlignment.RIGHT, isBold = true)
        }


        // --- 5. TOTAL ROW ---
        // This row HAS borders, so it will effectively "Close" the table at the bottom
        fun addTotal(text: String, align: TextAlignment) {
            table.addCell(Cell().add(Paragraph(text)
                .setFontSize(dataSize)
                .setBold()
                .setFixedLeading(dataSize + 1)
            )
                .setTextAlignment(align)
                .setBorder(SolidBorder(0.5f)) // Full border to close the section
                .setPadding(tightPadding))
        }

        // Label Cell
        table.addCell(Cell().add(Paragraph("Total")
            .setFontSize(dataSize)
            .setBold()
            .setFixedLeading(dataSize + 1)
        )
            .setTextAlignment(TextAlignment.RIGHT)
            .setBorder(SolidBorder(0.5f))
            .setPadding(tightPadding))

        // Values
        addTotal("%.2f".format(totalTaxable), TextAlignment.RIGHT)

        if (isInterState) {
            addTotal("", TextAlignment.CENTER)
            addTotal("%.2f".format(totalTaxAmt), TextAlignment.RIGHT)
        } else {
            addTotal("", TextAlignment.CENTER)
            addTotal("%.2f".format(totalTaxAmt / 2), TextAlignment.RIGHT)
            addTotal("", TextAlignment.CENTER)
            addTotal("%.2f".format(totalTaxAmt / 2), TextAlignment.RIGHT)
        }

        addTotal("%.2f".format(totalTaxAmt), TextAlignment.RIGHT)

        document.add(table)

    }


    // ==========================================
    // SECTION 4: FOOTER
    // ==========================================
    private fun drawFooterSection(document: Document, invoice: Invoice, context: Context?) {

        val borderStyle = SolidBorder(0.5f)
        val fontSize = 8f
        val smallFontSize = 7f
        val tightPadding = 2f

        // MASTER CONTAINER
        val mainFooterTable = Table(UnitValue.createPercentArray(floatArrayOf(100f)))
        mainFooterTable.useAllAvailableWidth()
        mainFooterTable.setMarginTop(0f)


        // ==============================================================================
        // MAIN ROW 1: TOP SECTION
        // ==============================================================================

        val topSectionContainer = Cell().setBorder(borderStyle).setPadding(0f)
        val topInternalTable = Table(UnitValue.createPercentArray(floatArrayOf(100f))).useAllAvailableWidth()

        // --- Part A: Tax Amount in Words (Auto-Fit) ---
        val taxInWords = NumberToWords.convert(invoice.totalTaxAmount)
        val taxWordsCell = Cell().setBorder(Border.NO_BORDER).setPadding(tightPadding)
            .add(Paragraph("Tax Amount (in words) :").setFontSize(fontSize))
            // 🔥 AUTO-FIT: Max width ~520pts (Full Page)
            .add(createAutoFitParagraph(taxInWords, 520f, maxFontSize = fontSize, isBold = true))

        topInternalTable.addCell(taxWordsCell)

        // --- Part B: Split Section (Left: Declaration | Right: Bank) ---
        val splitTable = Table(UnitValue.createPercentArray(floatArrayOf(50f, 50f))).useAllAvailableWidth()

        // Left Side: Declaration
        val declarationCell = Cell().setBorder(Border.NO_BORDER).setPadding(tightPadding)
            .setVerticalAlignment(VerticalAlignment.TOP)
            .setHeight(48f) // Min height to keep alignment

        declarationCell.add(Paragraph("Declaration").setFontSize(fontSize).setUnderline())
        declarationCell.add(Paragraph("1. IF THE INVOICE IS NOT CLEARED WITHIN 30 DAYS\nINTEREST @ 2%/MONTH WILL BE CHARGED.")
            .setFontSize(smallFontSize).setFixedLeading(8f))
        val jurisdiction = (if (invoice.firm.city.isNotBlank()) invoice.firm.city else invoice.firm.state).uppercase()
        declarationCell.add(Paragraph("2. ALL DISPUTES WILL BE SETTLED IN $jurisdiction\nCOURT ONLY.")
            .setFontSize(smallFontSize).setFixedLeading(8f).setMarginTop(2f))

        splitTable.addCell(declarationCell)

        // Right Side: Bank Details (Auto-Fit Values)
        val bankCell = Cell().setBorder(Border.NO_BORDER).setPadding(tightPadding)
            .setVerticalAlignment(VerticalAlignment.TOP)

        bankCell.add(Paragraph("Company's Bank Details").setFontSize(fontSize).setUnderline())

        // Nested Bank Table (Labels 35% | Values 65%)
        val bankDetailsTable = Table(UnitValue.createPercentArray(floatArrayOf(35f, 65f))).useAllAvailableWidth()

        fun addBankRow(label: String, value: String) {
            // Label
            bankDetailsTable.addCell(Cell().setBorder(Border.NO_BORDER).setPadding(0.5f)
                .add(Paragraph(label).setFontSize(smallFontSize)))

            // Value 🔥 AUTO-FIT: Max width ~175pts (half page - margin)
            val fitValue = createAutoFitParagraph(
                ": $value",
                maxWidthPoints = 175f,
                maxFontSize = fontSize,
                minFontSize = 6f,
                isBold = true
            )

            bankDetailsTable.addCell(Cell().setBorder(Border.NO_BORDER).setPadding(0.5f)
                .add(fitValue))
        }

        // Data Mapping
        addBankRow("Bank Name", invoice.firm.bankName) // Or invoice.firm.bankName
        addBankRow("A/c No.", invoice.firm.accountNumber)          // Or invoice.firm.accountNumber
        addBankRow("Branch & IFS", "${invoice.firm.branchName} & ${invoice.firm.ifscCode}") // Or invoice.firm.ifsc

        bankCell.add(bankDetailsTable)
        splitTable.addCell(bankCell)

        topInternalTable.addCell(Cell().setBorder(Border.NO_BORDER).setPadding(0f).add(splitTable))
        topSectionContainer.add(topInternalTable)
        mainFooterTable.addCell(topSectionContainer)


        // ==============================================================================
        // MAIN ROW 2: BOTTOM SECTION (Signatories)
        // ==============================================================================

        val bottomSectionContainer = Cell().setBorder(borderStyle).setPadding(tightPadding)
            .setTextAlignment(TextAlignment.RIGHT)

        val bottomInternalTable = Table(UnitValue.createPercentArray(floatArrayOf(100f))).useAllAvailableWidth()
        bottomInternalTable.setTextAlignment(TextAlignment.RIGHT)

        // --- "for [Firm Name]" (Auto-Fit) ---
        // 🔥 AUTO-FIT: This ensures long firm names don't wrap to 2 lines
        val firmNameText = "for ${invoice.firm.tradeName}"
        val firmNamePara = createAutoFitParagraph(
            firmNameText,
            maxWidthPoints = 250f, // Approx half page width
            maxFontSize = fontSize,
            isBold = true
        ).setTextAlignment(TextAlignment.RIGHT)

        bottomInternalTable.addCell(Cell().setBorder(Border.NO_BORDER).setPadding(0f)
            .add(firmNamePara))

        // --- Signature Image Space ---
        val signImageCell = Cell().setBorder(Border.NO_BORDER).setHeight(40f).setPadding(0f)
        try {
            if (context != null) {
                // Image loading logic goes here...
            }
        } catch (e: Exception) { }
        bottomInternalTable.addCell(signImageCell)

        // --- Signatory Labels ---
        val labelsTable = Table(UnitValue.createPercentArray(floatArrayOf(33f, 33f, 34f))).useAllAvailableWidth()

        fun addLabel(text: String, align: TextAlignment) {
            labelsTable.addCell(Cell().setBorder(Border.NO_BORDER).setPadding(0f).setPaddingTop(5f)
                .setTextAlignment(align)
                .add(Paragraph(text).setFontSize(fontSize)))
        }
        addLabel("Prepared by", TextAlignment.LEFT)
        addLabel("Verified by", TextAlignment.CENTER)
        addLabel("Authorised Signatory", TextAlignment.RIGHT)

        bottomInternalTable.addCell(Cell().setBorder(Border.NO_BORDER).setPadding(0f).add(labelsTable))
        bottomSectionContainer.add(bottomInternalTable)
        mainFooterTable.addCell(bottomSectionContainer)


        // Final Add
        document.add(mainFooterTable)

        // Disclaimer
        document.add(Paragraph("This is a Computer Generated Invoice")
            .setFontSize(smallFontSize).setTextAlignment(TextAlignment.CENTER).setMarginTop(2f))
    }


//---------------------------------------Helper Functions---------------------------------------//
    private fun drawPartyCell( title: String?, name: String, addressLine1: String, addressLine2: String?,  city: String, stateName: String, pincode: String, gstin: String, stateCode: String, fixedHeight: Float, isBoldName: Boolean): Cell {

        val maxNameSize = 10f
        val addrSize = 9f
        val minSize = 6f
        val safeWidth = 320f

        val cell = Cell().setPadding(3f).setHeight(fixedHeight).setBorder(SolidBorder(0.5f)).setBorderRight(Border.NO_BORDER)
        // No Padding Bottom needed if we are stacking normally

        // Title
        if (title != null) cell.add(Paragraph(title).setFontSize(7f).setFixedLeading(8f))

        // Name
        cell.add(createAutoFitParagraph(name, safeWidth, maxNameSize, minSize, isBoldName))

        // Address Logic (Combined + Split if overflow)
        val addr1 = if (addressLine1.isNotBlank()) addressLine1 else ""
        val addr2 = if (!addressLine2.isNullOrBlank()) addressLine2 else ""

        val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        val w1 = font.getWidth(addr1, addrSize)
        val w2 = font.getWidth(addr2, addrSize)

        if (w1 <= safeWidth && w2 <= safeWidth) {
            // Standard
            val t1 = if(addr1.isNotBlank()) addr1 else "\u00A0"
            cell.add(Paragraph(t1).setFontSize(addrSize).setFixedLeading(addrSize + 1f))
            val t2 = if(addr2.isNotBlank()) addr2 else "\u00A0"
            cell.add(Paragraph(t2).setFontSize(addrSize).setFixedLeading(addrSize + 1f))
        } else {
            // Merged
            val fullAddress = listOf(addr1, addr2).filter { it.isNotBlank() }.joinToString(", ")
            val (line1, line2, bestSize) = calculateSmartAddressSplit(fullAddress, safeWidth, addrSize, minSize)
            cell.add(Paragraph(line1).setFontSize(bestSize).setFixedLeading(bestSize + 1f))
            val line2Text = if(line2.isNotBlank()) line2 else "\u00A0"
            cell.add(Paragraph(line2Text).setFontSize(bestSize).setFixedLeading(bestSize + 1f))
        }

        // City
        val cityPin = listOf(city, stateName).filter { it.isNotBlank() }.joinToString(", ") + if (pincode.isNotBlank()) " - $pincode" else ""
        val cityText = if (cityPin.isNotBlank()) cityPin else "\u00A0"
        cell.add(createAutoFitParagraph(cityText, safeWidth, 9f, minSize))

        // GSTIN (Stacked Normally)
        val gstinText = if (gstin.isNotBlank()) "GSTIN/UIN : $gstin" else "GSTIN/UIN :"
        cell.add(createAutoFitParagraph(gstinText, safeWidth, 9f, minSize))

        // State (Stacked Normally)
        val codeDisplay = if (stateCode.isNotBlank()) ", Code : $stateCode" else ", Code :"
        val stateText = "State Name : $stateName$codeDisplay"
        cell.add(createAutoFitParagraph(stateText, safeWidth, 9f, minSize))

        return cell
    }

    private fun drawMetaCell(label: String, value: String): Cell {
        val cell = Cell().setBorder(SolidBorder(0.5f)).setPadding(2f).setHeight(21f)
        cell.add(Paragraph(label).setFontSize(7f).setFontColor(ColorConstants.DARK_GRAY).setFixedLeading(7f).setMarginBottom(2.5f))
        val safeValue = createAutoFitParagraph(value, 100f, 9f, 6f, true)
        cell.add(safeValue)
        return cell
    }

    private fun calculateSmartAddressSplit( fullText: String,  maxWidth: Float,  maxSize: Float,  minSize: Float): Triple<String, String, Float> {
        val font = PdfFontFactory.createFont(StandardFonts.HELVETICA)
        var currentSize = maxSize

        // Loop to find best fit size
        while (currentSize >= minSize) {
            val words = fullText.split(" ")
            val line1 = StringBuilder()
            val line2 = StringBuilder()

            for (word in words) {
                val testLine = if (line1.isEmpty()) word else "$line1 $word"
                if (font.getWidth(testLine, currentSize) < maxWidth) {
                    if (line1.isNotEmpty()) line1.append(" ")
                    line1.append(word)
                } else {
                    if (line2.isNotEmpty()) line2.append(" ")
                    line2.append(word)
                }
            }

            if (font.getWidth(line2.toString(), currentSize) <= maxWidth) {
                return Triple(line1.toString(), line2.toString(), currentSize)
            }
            currentSize -= 0.5f
        }
        return Triple(fullText, "", minSize) // Fallback
    }

    private fun createAutoFitParagraph( text: String, maxWidthPoints: Float, maxFontSize: Float = 9f, minFontSize: Float = 7f, isBold: Boolean = false): Paragraph {

        // 1. Load Font for measurement
        val font = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD)
        val safeText = text.replace("\n", " ") // Measure single line width

        // 2. Calculate Best Font Size
        var currentSize = maxFontSize
        var textWidth = font.getWidth(safeText, currentSize)

        // Simple reduction loop
        while (textWidth > maxWidthPoints && currentSize > minFontSize) {
            currentSize -= 0.5f
            textWidth = font.getWidth(safeText, currentSize)
        }

        // 3. Return Styled Paragraph
        val p = Paragraph(text)
            .setFontSize(currentSize)
            .setFixedLeading(currentSize + 1f)

        if (isBold) p.setBold()

        return p
    }




}
