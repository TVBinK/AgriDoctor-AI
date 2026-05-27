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
    
    @Query("SELECT * FROM diagnose_results WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestDiagnoseResult(ownerUserId: String): DiagnoseResultEntity?
    
    @Query("SELECT * FROM diagnose_results WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC")
    suspend fun getAllDiagnoseResults(ownerUserId: String): List<DiagnoseResultEntity>

    @Query("SELECT * FROM diagnose_results WHERE ownerUserId = :ownerUserId ORDER BY timestamp DESC")
    fun observeAllDiagnoseResults(ownerUserId: String): kotlinx.coroutines.flow.Flow<List<DiagnoseResultEntity>>

    @Query("SELECT serverHistoryId FROM diagnose_results WHERE ownerUserId = :ownerUserId AND serverHistoryId IS NOT NULL")
    suspend fun getServerHistoryIds(ownerUserId: String): List<String>
    
    @Query("DELETE FROM diagnose_results WHERE id = :id")
    suspend fun deleteDiagnoseResult(id: Long)
    
    @Query("DELETE FROM diagnose_results WHERE ownerUserId = :ownerUserId")
    suspend fun deleteAllDiagnoseResults(ownerUserId: String)
}

