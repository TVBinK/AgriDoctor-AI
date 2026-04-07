package com.baothanhbin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baothanhbin.core.database.model.PlantEntity

@Dao
interface PlantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlant(plant: PlantEntity): Long

    @Query("SELECT * FROM plants WHERE id = :id")
    suspend fun getPlantById(id: Long): PlantEntity?

    @Query("SELECT * FROM plants ORDER BY timestamp DESC")
    fun getAllPlants(): kotlinx.coroutines.flow.Flow<List<PlantEntity>>

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlant(id: Long)

    @Query("DELETE FROM plants")
    suspend fun deleteAllPlants()
    
    @Query("SELECT * FROM plants ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPlant(): PlantEntity?
}
