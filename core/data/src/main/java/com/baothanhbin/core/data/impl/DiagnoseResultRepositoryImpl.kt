package com.baothanhbin.core.data.impl

import android.util.Log
import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.datastore.AuthDataStore
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flatMapLatest

@OptIn(ExperimentalCoroutinesApi::class)
class DiagnoseResultRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase,
    private val authDataStore: AuthDataStore
) : DiagnoseResultRepository {

    companion object {
        private const val TAG = "DiagnoseResultRepository"
    }

    override suspend fun insertDiagnoseResult(diagnoseResult: DiagnoseResultEntity): Long {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return database.diagnoseResultDao().insertDiagnoseResult(
            diagnoseResult.copy(
                ownerUserId = diagnoseResult.ownerUserId.ifBlank { currentUserId }
            )
        )
    }

    override fun observeAllDiagnoseResults(): Flow<List<DiagnoseResultEntity>> {
        return authDataStore.currentUserIdFlow().flatMapLatest { currentUserId ->
            if (currentUserId.isNullOrBlank()) {
                flowOf(emptyList<DiagnoseResultEntity>())
            } else {
                database.diagnoseResultDao().observeAllDiagnoseResults(currentUserId)
            }
        }
    }

    override suspend fun getDiagnoseResultById(id: Long): DiagnoseResultEntity? {
        return database.diagnoseResultDao().getDiagnoseResultById(id)
    }

    override suspend fun getLatestDiagnoseResult(): DiagnoseResultEntity? {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return database.diagnoseResultDao().getLatestDiagnoseResult(currentUserId)
    }

    override suspend fun getAllDiagnoseResults(): List<DiagnoseResultEntity> {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return database.diagnoseResultDao().getAllDiagnoseResults(currentUserId)
    }

    override suspend fun getExistingServerHistoryIds(): Set<String> {
        val currentUserId = authDataStore.getUserId().orEmpty()
        return database.diagnoseResultDao().getServerHistoryIds(currentUserId).toSet()
    }

    override suspend fun deleteDiagnoseResult(id: Long) {
        database.diagnoseResultDao().deleteDiagnoseResult(id)
    }

    override suspend fun deleteAllDiagnoseResults() {
        val currentUserId = authDataStore.getUserId().orEmpty()
        database.diagnoseResultDao().deleteAllDiagnoseResults(currentUserId)
    }
}
