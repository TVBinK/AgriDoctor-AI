package com.baothanhbin.core.data.di

import android.content.Context
import com.baothanhbin.core.data.encryption.KeyEncryptionManager
import com.baothanhbin.core.data.impl.DiagnoseResultRepositoryImpl
import com.baothanhbin.core.data.repository.ApiKeyRepository
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.data.repository.SecuritySettingsRepository
import com.baothanhbin.core.datastore.ApiKeyDataStore
import com.baothanhbin.core.datastore.SecuritySettingsDataStore
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

    @Binds
    @Singleton
    abstract fun bindPlantRepository(
        impl: com.baothanhbin.core.data.impl.PlantRepositoryImpl
    ): com.baothanhbin.core.data.repository.PlantRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: com.baothanhbin.core.data.impl.AuthRepositoryImpl
    ): com.baothanhbin.core.data.repository.AuthRepository
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
    fun provideSecuritySettingsDataStore(
        @ApplicationContext context: Context
    ): SecuritySettingsDataStore {
        return SecuritySettingsDataStore(context)
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
        encryptionManager: KeyEncryptionManager,
        authRepository: com.baothanhbin.core.data.repository.AuthRepository
    ): ApiKeyRepository {
        return ApiKeyRepository(
            dataStore = dataStore,
            encryptionManager = encryptionManager,
            networkDataSource = com.baothanhbin.core.network.NetworkDataSource,
            authRepository = authRepository
        )
    }

    @Provides
    @Singleton
    fun provideSecuritySettingsRepository(
        securitySettingsDataStore: SecuritySettingsDataStore
    ): SecuritySettingsRepository {
        return SecuritySettingsRepository(securitySettingsDataStore)
    }
}
