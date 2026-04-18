package com.baothanhbin.core.data.impl

import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity
import javax.inject.Inject

class PlantRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase
) : PlantRepository {
    private val plantDao get() = database.plantDao()
    private val reminderDao get() = database.reminderDao()

    override suspend fun insertPlant(plant: PlantEntity): Long {
        return plantDao.insertPlant(plant)
    }

    override suspend fun getPlantById(id: Long): PlantEntity? {
        return plantDao.getPlantById(id)
    }

    override suspend fun getLatestPlant(): PlantEntity? {
        return plantDao.getLatestPlant()
    }

    override fun getAllPlants(): kotlinx.coroutines.flow.Flow<List<PlantEntity>> {
        return plantDao.getAllPlants()
    }

    override suspend fun deletePlant(id: Long) {
        plantDao.deletePlant(id)
    }

    override suspend fun deleteAllPlants() {
        plantDao.deleteAllPlants()
    }

    override suspend fun insertReminder(reminder: ReminderEntity): Long {
        return reminderDao.insertReminder(reminder)
    }

    override fun getAllReminders(): kotlinx.coroutines.flow.Flow<List<ReminderEntity>> {
        return reminderDao.getAllReminders()
    }

    override suspend fun getReminderById(id: Long): ReminderEntity? {
        return reminderDao.getReminderById(id)
    }

    override suspend fun updateReminderStatus(id: Long, isCompleted: Boolean) {
        reminderDao.updateReminderStatus(id, isCompleted)
    }

    override suspend fun deleteReminder(id: Long) {
        reminderDao.deleteReminder(id)
    }
}
