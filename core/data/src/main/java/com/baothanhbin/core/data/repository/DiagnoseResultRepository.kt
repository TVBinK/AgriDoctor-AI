package com.baothanhbin.core.data.repository

import com.baothanhbin.core.database.model.DiagnoseResultEntity

interface DiagnoseResultRepository {
    suspend fun insertDiagnoseResult(diagnoseResult: DiagnoseResultEntity): Long
    
    suspend fun getDiagnoseResultById(id: Long): DiagnoseResultEntity?
    
    suspend fun getLatestDiagnoseResult(): DiagnoseResultEntity?
    
    suspend fun getAllDiagnoseResults(): List<DiagnoseResultEntity>
    
    suspend fun deleteDiagnoseResult(id: Long)
    
    suspend fun deleteAllDiagnoseResults()
}
