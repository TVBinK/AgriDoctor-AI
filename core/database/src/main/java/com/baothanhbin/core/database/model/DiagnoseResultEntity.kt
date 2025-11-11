package com.baothanhbin.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.baothanhbin.core.database.converter.TreatmentDataListConverter
import com.baothanhbin.core.database.converter.RecoveryCareDataListConverter
import com.baothanhbin.core.database.converter.StringListConverter
import com.baothanhbin.core.model.DiagnoseData
import com.baothanhbin.core.model.RecoveryCareData
import com.baothanhbin.core.model.TreatmentData

/**
 * Room entity for caching disease diagnosis results
 */
@Entity(tableName = "diagnose_results")
data class DiagnoseResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val diseaseName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null, // URI của ảnh đã chụp hoặc chọn từ thư viện
    val location: String? = null, // Địa chỉ vị trí khi chụp ảnh
    @TypeConverters(StringListConverter::class)
    val possibleProblems: List<String>,
    val symptoms: String,
    val causes: String,
    @TypeConverters(TreatmentDataListConverter::class)
    val treatment: List<TreatmentData>,
    @TypeConverters(RecoveryCareDataListConverter::class)
    val recoveryCare: List<RecoveryCareData>
) {
    fun toDiagnoseData(): DiagnoseData {
        return DiagnoseData(
            diseaseName = diseaseName,
            possibleProblems = possibleProblems,
            symptoms = symptoms,
            causes = causes,
            treatment = treatment,
            recoveryCare = recoveryCare
        )
    }
}

fun DiagnoseData.toEntity(imageUri: String? = null, location: String? = null): DiagnoseResultEntity {
    return DiagnoseResultEntity(
        diseaseName = diseaseName,
        imageUri = imageUri,
        location = location,
        possibleProblems = possibleProblems,
        symptoms = symptoms,
        causes = causes,
        treatment = treatment,
        recoveryCare = recoveryCare
    )
}

