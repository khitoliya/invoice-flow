package com.dollyplastic.invoiceapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dollyplastic.invoiceapp.data.local.converters.InvoiceTypeConverters
import com.dollyplastic.invoiceapp.data.local.dao.*
import com.dollyplastic.invoiceapp.data.models.*

@Database(
    entities = [
        Invoice::class,
        Purchase::class,
        Firm::class,
        Party::class,
        Item::class,
        PincodeDistance::class,
        ConfigEntity::class
    ],
    version = 8,
    exportSchema = false
)
@TypeConverters(InvoiceTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun invoiceDao(): InvoiceDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun firmDao(): FirmDao
    abstract fun partyDao(): PartyDao
    abstract fun itemDao(): ItemDao
    abstract fun pincodeDistanceDao(): PincodeDistanceDao
    abstract fun configDao(): ConfigDao
}
