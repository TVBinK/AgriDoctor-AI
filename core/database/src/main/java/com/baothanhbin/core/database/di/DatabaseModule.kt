package com.baothanhbin.core.database.di

import android.content.Context
import androidx.room.Room
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.MIGRATION_12_13
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val DATABASE_NAME = "agridoctor_database"
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AgriDoctorDatabase {
        return Room.databaseBuilder(context, AgriDoctorDatabase::class.java, DATABASE_NAME)
            .addMigrations(MIGRATION_12_13)
            .fallbackToDestructiveMigration()
            .build()
    }
}
