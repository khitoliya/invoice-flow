package com.dollyplastic.invoiceapp.di

import com.dollyplastic.invoiceapp.data.repository.FirmRepository
import com.dollyplastic.invoiceapp.data.repository.FirmRepositoryImpl
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepository
import com.dollyplastic.invoiceapp.data.repository.InvoiceRepositoryImpl
import com.dollyplastic.invoiceapp.data.repository.ItemRepository
import com.dollyplastic.invoiceapp.data.repository.ItemRepositoryImpl
import com.dollyplastic.invoiceapp.data.repository.PartyRepository
import com.dollyplastic.invoiceapp.data.repository.PartyRepositoryImpl
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFirmRepository(
        firestore: FirebaseFirestore
    ): FirmRepository =
        FirmRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun providePartyRepository(
        firestore: FirebaseFirestore
    ): PartyRepository =
        PartyRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideItemRepository(
        firestore: FirebaseFirestore
    ): ItemRepository =
        ItemRepositoryImpl(firestore)

    @Provides
    @Singleton
    fun provideInvoiceRepository(
        firestore: FirebaseFirestore
    ): InvoiceRepository =
        InvoiceRepositoryImpl(firestore)
}
