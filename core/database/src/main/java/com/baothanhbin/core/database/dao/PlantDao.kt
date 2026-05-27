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

    @Query("SELECT * FROM plants WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC")
    fun getAllPlants(ownerUserId: String): kotlinx.coroutines.flow.Flow<List<PlantEntity>>

    @Query("SELECT * FROM plants WHERE ownerUserId = :ownerUserId AND sourceType = :sourceType ORDER BY timestamp DESC")
    fun getPlantsBySource(
        ownerUserId: String,
        sourceType: String
    ): kotlinx.coroutines.flow.Flow<List<PlantEntity>>

    @Query("SELECT serverHistoryId FROM plants WHERE ownerUserId = :ownerUserId AND serverHistoryId IS NOT NULL")
    suspend fun getServerHistoryIds(ownerUserId: String): List<String>

    @Query("DELETE FROM plants WHERE id = :id")
    suspend fun deletePlant(id: Long)

    @Query("DELETE FROM plants WHERE ownerUserId = :ownerUserId")
    suspend fun deleteAllPlants(ownerUserId: String)
    
    @Query("SELECT * FROM plants WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPlant(ownerUserId: String): PlantEntity?
}
