package com.baothanhbin.agridoctorai.di

import com.baothanhbin.agridoctorai.JniSecretProvider
import com.baothanhbin.core.network.SecretProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSecretProvider(): SecretProvider {
        return JniSecretProvider
    }
}

