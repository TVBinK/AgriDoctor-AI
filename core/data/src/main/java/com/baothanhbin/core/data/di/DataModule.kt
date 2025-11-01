package com.baothanhbin.core.data.di

import com.baothanhbin.core.data.impl.DiagnoseResultRepositoryImpl
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
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
