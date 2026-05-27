package com.baothanhbin.core.data.repository

import com.baothanhbin.core.database.model.DiagnoseResultEntity
import kotlinx.coroutines.flow.Flow

interface DiagnoseResultRepository {
    suspend fun insertDiagnoseResult(diagnoseResult: DiagnoseResultEntity): Long
    fun observeAllDiagnoseResults(): Flow<List<DiagnoseResultEntity>>
    
    suspend fun getDiagnoseResultById(id: Long): DiagnoseResultEntity?
    
    suspend fun getLatestDiagnoseResult(): DiagnoseResultEntity?
    
    suspend fun getAllDiagnoseResults(): List<DiagnoseResultEntity>

    suspend fun getExistingServerHistoryIds(): Set<String>
    
    suspend fun deleteDiagnoseResult(id: Long)
    
    suspend fun deleteAllDiagnoseResults()
}
