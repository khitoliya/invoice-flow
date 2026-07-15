package com.dollyplastic.invoiceapp.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.domain.Parsing.ResultParser
import com.dollyplastic.invoiceapp.domain.Workflow.DefaultInvoiceStatusUpdater
import com.dollyplastic.invoiceapp.domain.Workflow.InvoiceStatusUpdater
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import androidx.room.Room
import com.dollyplastic.invoiceapp.data.local.AppDatabase
import com.dollyplastic.invoiceapp.data.repository.AppLockPreferences
import com.dollyplastic.invoiceapp.data.repository.AppLockRepository

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth =
        FirebaseAuth.getInstance()

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore =
        FirebaseFirestore.getInstance()

    @Provides
    @Singleton
    fun provideCredentialRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.dollyplastic.invoiceapp.data.credentials.CredentialRepository =
        com.dollyplastic.invoiceapp.data.credentials.CredentialRepository(context)

    @Provides
    @Singleton
    fun provideResultParser(): ResultParser = ResultParser

    @Provides
    @Singleton
    fun provideInvoiceStatusUpdater(repository: InvoiceRepository): InvoiceStatusUpdater =
        DefaultInvoiceStatusUpdater(repository)

    @Provides
    @Singleton
    fun provideSettingsRepository(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): com.dollyplastic.invoiceapp.data.settings.SettingsRepository =
        com.dollyplastic.invoiceapp.data.settings.SettingsRepository(context)

    private val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
        override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
            // Add invoiceDateEpoch column
            database.execSQL("ALTER TABLE invoices ADD COLUMN invoiceDateEpoch INTEGER NOT NULL DEFAULT 0")
            // Create Indices
            database.execSQL("CREATE INDEX IF NOT EXISTS index_invoices_invoiceNumber ON invoices(invoiceNumber)")
            database.execSQL("CREATE INDEX IF NOT EXISTS index_invoices_invoiceDateEpoch ON invoices(invoiceDateEpoch)")
            // Re-create existing indices if needed? Room handles Index changes usually if table structure matches.
            // But we explicitly added them in Entity.
            
            // Note: firmGstin index was already added in V5.
        }
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@dagger.hilt.android.qualifiers.ApplicationContext context: android.content.Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "invoice_app_db"
        )
        .addMigrations(MIGRATION_5_6)
        .fallbackToDestructiveMigration() // Still keep as backstop for other versions
        .build()

    @Provides
    fun provideInvoiceDao(db: AppDatabase) = db.invoiceDao()

    @Provides
    fun providePurchaseDao(db: AppDatabase) = db.purchaseDao()

    @Provides
    fun provideFirmDao(db: AppDatabase) = db.firmDao()

    @Provides
    fun providePartyDao(db: AppDatabase) = db.partyDao()

    @Provides
    fun provideItemDao(db: AppDatabase) = db.itemDao()


    
    @Provides
    fun provideConfigDao(db: AppDatabase) = db.configDao()

    @Provides
    fun providePincodeDistanceDao(db: AppDatabase) = db.pincodeDistanceDao()

    @Provides
    @Singleton
    fun provideAppLockRepository(
        prefs: AppLockPreferences
    ): AppLockRepository = AppLockRepository(prefs)

}
