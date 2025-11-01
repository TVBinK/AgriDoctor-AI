package com.baothanhbin.core.data.impl

import com.baothanhbin.core.data.repository.DiagnoseResultRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.model.DiagnoseResultEntity
import javax.inject.Inject

class DiagnoseResultRepositoryImpl @Inject constructor(
    private val database: AgriDoctorDatabase
) : DiagnoseResultRepository {

    override suspend fun insertDiagnoseResult(diagnoseResult: DiagnoseResultEntity): Long {
        return database.diagnoseResultDao().insertDiagnoseResult(diagnoseResult)
    }

    override suspend fun getDiagnoseResultById(id: Long): DiagnoseResultEntity? {
        return database.diagnoseResultDao().getDiagnoseResultById(id)
    }

    override suspend fun getLatestDiagnoseResult(): DiagnoseResultEntity? {
        return database.diagnoseResultDao().getLatestDiagnoseResult()
    }

    override suspend fun getAllDiagnoseResults(): List<DiagnoseResultEntity> {
        return database.diagnoseResultDao().getAllDiagnoseResults()
    }

    override suspend fun deleteDiagnoseResult(id: Long) {
        database.diagnoseResultDao().deleteDiagnoseResult(id)
    }

    override suspend fun deleteAllDiagnoseResults() {
        database.diagnoseResultDao().deleteAllDiagnoseResults()
    }
}
