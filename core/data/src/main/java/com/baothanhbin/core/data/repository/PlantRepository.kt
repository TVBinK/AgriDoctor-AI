package com.baothanhbin.core.data.repository

import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.ReminderEntity

interface PlantRepository {
    suspend fun insertPlant(plant: PlantEntity): Long
    suspend fun getPlantById(id: Long): PlantEntity?
    suspend fun getLatestPlant(): PlantEntity?
    fun getAllPlants(): kotlinx.coroutines.flow.Flow<List<PlantEntity>>
    fun getRecognizedPlants(): kotlinx.coroutines.flow.Flow<List<PlantEntity>>
    suspend fun getExistingServerHistoryIds(): Set<String>
    suspend fun deletePlant(id: Long)
    suspend fun deleteAllPlants()
    
    suspend fun insertReminder(reminder: ReminderEntity): Long
    fun getAllReminders(): kotlinx.coroutines.flow.Flow<List<ReminderEntity>>
    suspend fun getReminderById(id: Long): ReminderEntity?
    suspend fun updateReminderStatus(id: Long, isCompleted: Boolean)
    suspend fun deleteReminder(id: Long)
}
