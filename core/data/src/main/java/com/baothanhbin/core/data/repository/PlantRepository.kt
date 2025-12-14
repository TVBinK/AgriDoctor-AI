package com.baothanhbin.core.data.repository

import com.baothanhbin.core.database.model.PlantEntity

interface PlantRepository {
    suspend fun insertPlant(plant: PlantEntity): Long
    suspend fun getPlantById(id: Long): PlantEntity?
    suspend fun getLatestPlant(): PlantEntity?
    suspend fun getAllPlants(): List<PlantEntity>
    suspend fun deletePlant(id: Long)
    suspend fun deleteAllPlants()
}
