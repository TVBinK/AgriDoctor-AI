package com.baothanhbin.core.data.di

import android.content.Context
import com.baothanhbin.core.data.impl.AuthRepositoryImpl
import com.baothanhbin.core.data.impl.DiagnoseResultRepositoryImpl
import com.baothanhbin.core.data.impl.PlantRepositoryImpl
import com.baothanhbin.core.data.repository.ApiKeyRepository
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.datastore.ApiKeyDataStore
import com.baothanhbin.core.datastore.AuthDataStore
import com.baothanhbin.core.network.NetworkDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Đảm bảo rằng các repository được cung cấp ở cấp độ ứng dụng
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindDiagnoseResultRepository(
        impl: DiagnoseResultRepositoryImpl
    ): DiagnoseResultRepository

    @Binds
    @Singleton
    abstract fun bindPlantRepository(
        impl: PlantRepositoryImpl
    ): PlantRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository
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
    ): AuthDataStore {
        return AuthDataStore(context)
    }

    @Provides
    @Singleton
    fun provideApiKeyRepository(
        dataStore: ApiKeyDataStore,
        authRepository: AuthRepository
    ): ApiKeyRepository {
        return ApiKeyRepository(
            dataStore = dataStore,
            networkDataSource = NetworkDataSource,
            authRepository = authRepository
        )
    }
}
