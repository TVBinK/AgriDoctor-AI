package com.baothanhbin.core.database.di

import android.content.Context
import androidx.room.Room
import com.baothanhbin.core.database.AgriDoctorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AgriDoctorDatabase {
        return Room.databaseBuilder(
            context,
            AgriDoctorDatabase::class.java,
            "agridoctor_database"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }
}