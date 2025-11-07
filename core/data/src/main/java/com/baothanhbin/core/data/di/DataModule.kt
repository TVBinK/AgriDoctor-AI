package com.baothanhbin.core.data.di

import android.content.Context
import com.baothanhbin.core.datastore.ApiKeyDataStore
import com.baothanhbin.core.data.encryption.KeyEncryptionManager
import com.baothanhbin.core.data.impl.DiagnoseResultRepositoryImpl
import com.baothanhbin.core.data.repository.ApiKeyRepository
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindDiagnoseResultRepository(
        impl: DiagnoseResultRepositoryImpl
    ): DiagnoseResultRepository
}

@Module
@InstallIn(SingletonComponent::class)
object ApiKeyModule {

    @Provides
    @Singleton
    fun provideApiKeyDataStore(
        @ApplicationContext context: Context
    ): ApiKeyDataStore {
        return ApiKeyDataStore(context)
    }

    @Provides
    @Singleton
    fun provideKeyEncryptionManager(
        @ApplicationContext context: Context
    ): KeyEncryptionManager {
        return KeyEncryptionManager(context)
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        dataStore: ApiKeyDataStore,
        encryptionManager: KeyEncryptionManager
    ): ApiKeyRepository {
        return ApiKeyRepository(
            dataStore = dataStore,
            encryptionManager = encryptionManager,
            networkDataSource = com.baothanhbin.core.network.NetworkDataSource
        )
    }
}
