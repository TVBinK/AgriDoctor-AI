package com.baothanhbin.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.baothanhbin.core.database.converter.StringListConverter
import com.baothanhbin.core.model.ClassifyData

@Entity(tableName = "plants")
data class PlantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val plantName: String,
    val plantNameVN: String? = null,
    val confidence: Double? = null,
    val description: String? = null,
    val scientificName: String? = null,
    val family: String? = null,
    val season: String? = null,
    val imageUri: String? = null,
    val location: String? = null,
    val timestamp: Long = System.currentTimeMillis(),

    @TypeConverters(StringListConverter::class)
    val commonNames: List<String> = emptyList(),

    @TypeConverters(StringListConverter::class)
    val growingRegions: List<String> = emptyList(),

    @TypeConverters(StringListConverter::class)
    val careTips: List<String> = emptyList(),

    @TypeConverters(StringListConverter::class)
    val commonDiseases: List<String> = emptyList()
)

fun ClassifyData.toPlantEntity(imageUri: String? = null, location: String? = null): PlantEntity {
    return PlantEntity(
        plantName = plantName,
        plantNameVN = plantNameVN,
        confidence = confidence,
        description = description,
        scientificName = scientificName,
        family = family,
        season = season,
        imageUri = imageUri,
        location = location,
        commonNames = commonNames ?: emptyList(),
        growingRegions = growingRegions ?: emptyList(),
        careTips = careTips ?: emptyList(),
        commonDiseases = commonDiseases ?: emptyList()
    )
}

fun PlantEntity.toClassifyData(): ClassifyData {
    return ClassifyData(
        plantName = plantName,
        plantNameVN = plantNameVN,
        confidence = confidence,
        description = description,
        scientificName = scientificName,
        family = family,
        season = season,
        commonNames = commonNames,
        growingRegions = growingRegions,
        careTips = careTips,
        commonDiseases = commonDiseases
    )
}
