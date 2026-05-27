package com.baothanhbin.core.data.impl

import com.baothanhbin.core.data.repository.PlantRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.PlantSourceType
import com.baothanhbin.core.database.model.ReminderEntity
import com.baothanhbin.core.datastore.AuthDataStore
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class PlantRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase,
    private val authDataStore: AuthDataStore
) : PlantRepository {
    private val plantDao get() = database.plantDao()
    private val reminderDao get() = database.reminderDao()

    override suspend fun insertPlant(plant: PlantEntity): Long {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return plantDao.insertPlant(
            plant.copy(ownerUserId = plant.ownerUserId.ifBlank { currentUserId })
        )
    }

    override suspend fun getPlantById(id: Long): PlantEntity? {
        return plantDao.getPlantById(id)
    }

    override suspend fun getLatestPlant(): PlantEntity? {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return plantDao.getLatestPlant(currentUserId)
    }

    override fun getAllPlants(): kotlinx.coroutines.flow.Flow<List<PlantEntity>> {
        return authDataStore.currentUserIdFlow().flatMapLatest { currentUserId ->
            if (currentUserId.isNullOrBlank()) {
                flowOf(emptyList<PlantEntity>())
            } else {
                plantDao.getAllPlants(currentUserId)
            }
        }
    }

    override fun getRecognizedPlants(): kotlinx.coroutines.flow.Flow<List<PlantEntity>> {
        return authDataStore.currentUserIdFlow().flatMapLatest { currentUserId ->
            if (currentUserId.isNullOrBlank()) {
                flowOf(emptyList<PlantEntity>())
            } else {
                plantDao.getPlantsBySource(currentUserId, PlantSourceType.RECOGNITION)
            }
        }
    }

    override suspend fun getExistingServerHistoryIds(): Set<String> {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return plantDao.getServerHistoryIds(currentUserId).toSet()
    }

    override suspend fun deletePlant(id: Long) {
        plantDao.deletePlant(id)
    }

    override suspend fun deleteAllPlants() {
        val currentUserId = authDataStore.getUserId().orEmpty()
        plantDao.deleteAllPlants(currentUserId)
    }

    override suspend fun insertReminder(reminder: ReminderEntity): Long {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return reminderDao.insertReminder(
            reminder.copy(ownerUserId = reminder.ownerUserId.ifBlank { currentUserId })
        )
    }

    override fun getAllReminders(): kotlinx.coroutines.flow.Flow<List<ReminderEntity>> {
        return authDataStore.currentUserIdFlow().flatMapLatest { currentUserId ->
            if (currentUserId.isNullOrBlank()) {
                flowOf(emptyList<ReminderEntity>())
            } else {
                reminderDao.getAllReminders(currentUserId)
            }
        }
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
