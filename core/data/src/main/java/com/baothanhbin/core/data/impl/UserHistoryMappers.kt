package com.baothanhbin.core.data.impl

import com.baothanhbin.core.database.model.DiagnoseResultEntity
import com.baothanhbin.core.database.model.PlantEntity
import com.baothanhbin.core.database.model.PlantSourceType
import com.baothanhbin.core.model.UserHistoryItem

fun UserHistoryItem.toDiagnoseResultEntity(imageUri: String?): DiagnoseResultEntity {
    return DiagnoseResultEntity(
        serverHistoryId = id,
        diseaseName = diseaseName.orEmpty(),
        timestamp = timestamp,
        imageUri = imageUri,
        possibleProblems = possibleProblems,
        symptoms = symptoms.orEmpty(),
        causes = causes.orEmpty(),
        treatment = treatment,
        recoveryCare = recoveryCare
    )
}

fun UserHistoryItem.toRecognizedPlantEntity(imageUri: String?): PlantEntity {
    return PlantEntity(
        serverHistoryId = id,
        sourceType = PlantSourceType.RECOGNITION,
        plantName = plantName.orEmpty(),
        plantNameVN = plantNameVN,
        confidence = confidence,
        description = description,
        scientificName = scientificName,
        family = family,
        season = season,
        imageUri = imageUri,
        timestamp = timestamp,
        commonNames = commonNames,
        growingRegions = growingRegions,
        careTips = careTips,
        commonDiseases = commonDiseases
    )
}
