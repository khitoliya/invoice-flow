package com.dollyplastic.invoiceapp.di

import com.dollyplastic.invoiceapp.data.repository.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindFirmRepository(
        impl: OfflineFirstFirmRepository
    ): FirmRepository

    @Binds
    @Singleton
    abstract fun bindPartyRepository(
        impl: OfflineFirstPartyRepository
    ): PartyRepository

    @Binds
    @Singleton
    abstract fun bindItemRepository(
        impl: OfflineFirstItemRepository
    ): ItemRepository

    @Binds
    @Singleton
    abstract fun bindInvoiceRepository(
        impl: OfflineFirstInvoiceRepository
    ): InvoiceRepository

    @Binds
    @Singleton
    abstract fun bindConfigRepository(
        impl: OfflineFirstConfigRepository
    ): ConfigRepository

    @Binds
    @Singleton
    abstract fun bindDistanceRepository(
        impl: OfflineFirstDistanceRepository
    ): DistanceRepository


}
