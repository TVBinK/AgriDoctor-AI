package com.baothanhbin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baothanhbin.core.database.model.DiagnoseResultEntity

@Dao
interface DiagnoseResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiagnoseResult(diagnoseResult: DiagnoseResultEntity): Long
    
    @Query("SELECT * FROM diagnose_results WHERE id = :id")
    suspend fun getDiagnoseResultById(id: Long): DiagnoseResultEntity?
    
    @Query("SELECT * FROM diagnose_results ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestDiagnoseResult(): DiagnoseResultEntity?
    
    @Query("SELECT * FROM diagnose_results ORDER BY timestamp DESC")
    suspend fun getAllDiagnoseResults(): List<DiagnoseResultEntity>
    
    @Query("DELETE FROM diagnose_results WHERE id = :id")
    suspend fun deleteDiagnoseResult(id: Long)
    
    @Query("DELETE FROM diagnose_results")
    suspend fun deleteAllDiagnoseResults()
}

