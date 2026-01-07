package com.dollyplastic.invoiceapp.pdf


import com.dollyplastic.invoiceapp.data.models.*
import com.dollyplastic.invoiceapp.domain.EwayBill.EWayBillDetails

object InvoiceTestFactory {

    // 1. THE "FULL LOAD" TEST (Matches Reference PDF Data)
    fun createFullInvoice(): Invoice {
        val items = getFullInvoiceItems()

        // Manual Tax Calculation for the Test Object
        // (In a real app, your ViewModel would calculate this)
        val taxableValue = items.sumOf { it.taxableValue }
        val igstAmount = taxableValue * 0.18 // Assuming 18% IGST

        return Invoice(
            invoiceNumber = "1245/25-26",
            invoiceDate = "17-Dec-2025",
            firm = getFirm(),
            billToParty = Party(
                tradeName = "DOLLY PLASTIC INDUSTRIES PVT LTD",
                addressLine1 = "H.No.1221, Mohalla Barsan, Shri Jagdev Singh Tillu Marg",
                addressLine2 = "Pole No. W326, Tikri Kalan",
                city = "New Delhi",
                state = "Delhi",
                pincode = "110041",
                gstin = "07ARDPK8616J1ZK",
                stateCode = "09"
            ),
            shipToParty = Party(
                tradeName = "DOLLY PLASTIC WAREHOUSE",
                addressLine1 = "Plot No 55, Udyog Vihar Phase IV",
                city = "Gurugram",
                state = "Haryana",
                pincode = "122015",
                gstin = "06ARDPK8616J1ZZ",
                stateCode = "09"
            ),

            // --- ITEMS & TAX ---
            items = items,
            totalTaxableValue = taxableValue,
            totalTaxAmount = igstAmount,
            totalInvoiceValue = taxableValue + igstAmount,

            eWayBillDetails = EWayBillDetails(ewayBillNo = "441660438080"),
            transportDetails = TransportDetails(
                transporterName = "TRANS GLOBAL CARRIER SERVICE",
                transporterDocNo = "GR-8821",
                transporterDocDate = "17-Dec-2025",
                vehicleNumber = "UP17-AT-3377",
                portOfDischarge = "MUNDRA PORT, GUJARAT"
            ),
            additionalDetails = AdditionalDetails(
                paymentMode = PaymentMode.RTGS,
                deliveryNoteNo = "DN/2025/099",
                deliveryNoteDate = "16-Dec-2025",
                buyerOrderNo = "PO/2025/DEC/004",
                referenceNo = "REF-9921",
                otherReferences = "Email dt. 15th Dec",
                termsOfDelivery = "CIF - Cost, Insurance, and Freight. Goods to be delivered at factory gate."
            ),
            generateEInvoice = true,
            eInvoiceDetails = EInvoiceDetails(
                irn = "c449f6aafdae85b218189a53a3f4be7e89d6be50942468fd705aedda1790aca2",
                ackNo = "123456789012345",
                ackDate = "17-Dec-2025 14:35:22",
                signedQrCode = "ThisIsATestStringForQRCodeGenerationInThePDF"
            )
        )
    }

    // 2. THE "CASH SALE" TEST (Single Item, Intra-State Tax)
    fun createCashSaleInvoice(): Invoice {
        val item = InvoiceItem(
            item = Item(name = "SCRAP PLASTIC MIX", hsnCode = "3915", unit = "KGS", gstRate = 18.0),
            quantity = 500.0,
            rate = 20.0,
            taxableValue = 10000.0,
            gstAmount = 1800.0
        )

        // Intra-state (CGST + SGST)
        val cgst = 900.0
        val sgst = 900.0

        return Invoice(
            invoiceNumber = "CS/001/25-26",
            invoiceDate = "19-Dec-2025",
            firm = getFirm(),
            isCashSale = true,
            billToParty = null,
            shipToParty = null,

            items = listOf(item),
            taxSummary = TaxSummary(igst = 0.0, cgst = cgst, sgst = sgst),
            totalTaxableValue = 10000.0,
            totalInvoiceValue = 11800.0,

            eWayBillDetails = null,
            transportDetails = TransportDetails(
                mode = TransportMode.ROAD,
                vehicleNumber = "DL-4C-NA-1234"
            ),
            additionalDetails = AdditionalDetails(
                paymentMode = PaymentMode.CASH,
                termsOfDelivery = "Ex-Works"
            )
        )
    }

    // 3. THE "MINIMAL" TEST (Simple B2B)
    fun createMinimalInvoice(): Invoice {
        val item = InvoiceItem(
            item = Item(name = "POLYMER RESIN A-GRADE", hsnCode = "3901", unit = "MT", gstRate = 18.0),
            quantity = 10.0,
            rate = 85000.0,
            taxableValue = 850000.0,
            gstAmount = 153000.0
        )

        return Invoice(
            invoiceNumber = "JBM/005",
            invoiceDate = "20-Dec-2025",
            firm = getFirm(),
            billToParty = Party(
                tradeName = "SIMPLE TRADERS",
                addressLine1 = "Shop No 5, Main Market",
                city = "Agra",
                state = "Uttar Pradesh",
                pincode = "282001",
                gstin = "09AAAAA0000A1Z5",
                stateCode = "09"
            ),
            shipToParty = null,

            items = listOf(item),
            taxSummary = TaxSummary(cgst = 76500.0, sgst = 76500.0, igst = 0.0), // Same state (UP to UP)
            totalTaxableValue = 850000.0,
            totalInvoiceValue = 1003000.0,

            eWayBillDetails = null,
            transportDetails = TransportDetails(),
            additionalDetails = AdditionalDetails(
                paymentMode = PaymentMode.CHEQUE
            )
        )
    }

    // --- HELPERS ---

    private fun getFirm(): Firm {
        return Firm(
            tradeName = "JAI BALAJI MAHARAJ POLYMERS INDIA PRIVATE LIMITED",
            addressLine1 = "Plot No. 19, Block-B",
            addressLine2 = "Roop Nagar Industrial Area Loni",
            city = "Ghaziabad",
            state = "Uttar Pradesh",
            pincode = "201102",
            gstin = "09ABFCS7562L1ZN",
            stateCode = "08"
        )
    }

    private fun getFullInvoiceItems(): List<InvoiceItem> {
        return listOf(
            InvoiceItem(
                item = Item(
                    name = "PLASTIC GRANULES-39021000",
                    hsnCode = "39021000",
                    unit = "KGS",
                    gstRate = 18.0
                ),
                quantity = 60000.00,
                rate = 36.00,
                taxableValue = 216000.00,
                gstAmount = 38880.00
            ),
            InvoiceItem(
                item = Item(
                    name = "PLASTIC GRANULES-39033000",
                    hsnCode = "39033000",
                    unit = "KGS",
                    gstRate = 18.0
                ),
                quantity = 4000.00,
                rate = 58.00,
                taxableValue = 99999.00,
                gstAmount = 2345.00
            )

        )
    }
}