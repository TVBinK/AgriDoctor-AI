package com.baothanhbin.core.data.di

import android.content.Context
import com.baothanhbin.core.data.impl.DiagnoseResultRepositoryImpl
import com.baothanhbin.core.data.repository.ApiKeyRepository
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.datastore.ApiKeyDataStore
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
    fun provideAuthDataStore(
        @ApplicationContext context: Context
    ): com.baothanhbin.core.datastore.AuthDataStore {
        return com.baothanhbin.core.datastore.AuthDataStore(context)
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        dataStore: ApiKeyDataStore,
        authRepository: com.baothanhbin.core.data.repository.AuthRepository
    ): ApiKeyRepository {
        return ApiKeyRepository(
            dataStore = dataStore,
            networkDataSource = com.baothanhbin.core.network.NetworkDataSource,
            authRepository = authRepository
        )
    }
}
