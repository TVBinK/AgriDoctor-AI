package com.baothanhbin.core.data.repository

import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity

interface PlantRepository {
    suspend fun insertPlant(plant: PlantEntity): Long
    suspend fun getPlantById(id: Long): PlantEntity?
    suspend fun getLatestPlant(): PlantEntity?
    suspend fun getAllPlants(): List<PlantEntity>
    suspend fun deletePlant(id: Long)
    suspend fun deleteAllPlants()
    
    suspend fun insertReminder(reminder: ReminderEntity): Long
    suspend fun getAllReminders(): List<ReminderEntity>
    suspend fun updateReminderStatus(id: Long, isCompleted: Boolean)
    suspend fun deleteReminder(id: Long)
}
