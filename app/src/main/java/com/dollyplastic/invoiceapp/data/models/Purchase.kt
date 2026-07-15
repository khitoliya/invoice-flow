package com.dollyplastic.invoiceapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey
    val purchaseId: String = "",              // internal UUID
    val firmId: String = "",                  // Link to User's Firm
    val vendorInvoiceNumber: String = "",     // Extracted ID
    val purchaseDate: String = "",            // Extracted Date
    
    val vendor: Party = Party(),              // SNAPSHOT of Vendor
    val vendorId: String = vendor.partyId,    // Extracted for Querying
    
    val items: List<PurchaseItem> = emptyList(),
    
    val totalTaxableValue: Double = 0.0,
    val totalTaxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    
    val pdfFilePath: String? = null,          // Path to imported PDF
    val importedAt: Long = System.currentTimeMillis()
)

data class PurchaseItem(
    val id: String = java.util.UUID.randomUUID().toString(),
    val name: String = "",
    val hsnCode: String = "",
    val quantity: Double = 0.0,
    val unit: String = "",
    val rate: Double = 0.0,
    val taxableValue: Double = 0.0,
    val taxRate: Double = 0.0,
    val taxAmount: Double = 0.0,
    val finalAmount: Double = 0.0
)
