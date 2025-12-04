package com.baothanhbin.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.baothanhbin.core.database.model.DiseaseListEntity

@Dao
interface DiseaseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDiseaseList(entity: DiseaseListEntity)

    @Query("SELECT * FROM diseases_cache LIMIT 1")
    suspend fun getLatestDiseaseList(): DiseaseListEntity?

    @Query("DELETE FROM diseases_cache")
    suspend fun clearDiseaseCache()
}


