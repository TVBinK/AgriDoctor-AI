package com.baothanhbin.core.data.impl

import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.model.PlantEntity
import javax.inject.Inject

class PlantRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase
) : PlantRepository {
    private val plantDao get() = database.plantDao()

    override suspend fun insertPlant(plant: PlantEntity): Long {
        return plantDao.insertPlant(plant)
    }

    override suspend fun getPlantById(id: Long): PlantEntity? {
        return plantDao.getPlantById(id)
    }

    override suspend fun getLatestPlant(): PlantEntity? {
        return plantDao.getLatestPlant()
    }

    override suspend fun getAllPlants(): List<PlantEntity> {
        return plantDao.getAllPlants()
    }

    override suspend fun deletePlant(id: Long) {
        plantDao.deletePlant(id)
    }

    override suspend fun deleteAllPlants() {
        plantDao.deleteAllPlants()
    }
}
