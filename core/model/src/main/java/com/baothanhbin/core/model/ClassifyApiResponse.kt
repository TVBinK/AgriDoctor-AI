package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

/**
 * API response model for plant classification
 */
@Serializable
data class ClassifyApiResponse(
    val success: Boolean,
    val data: ClassifyData
)

@Serializable
data class ClassifyData(
    val plantName: String,
    val plantNameVN: String? = null,
    val confidence: Double? = null, // Độ tin cậy (0.0 - 1.0)
    val classificationStatus: String? = null,
    val icon: String? = null,
    val description: String? = null,
    val scientificName: String? = null,
    val family: String? = null,
    val commonNames: List<String>? = null,
    val growingRegions: List<String>? = null,
    val season: String? = null,
    val careTips: List<String>? = null,
    val commonDiseases: List<String>? = null,
    val possiblePlants: List<PossiblePlant>? = null,
    val topPredictions: List<TopPrediction>? = null
)

@Serializable
data class PossiblePlant(
    val name: String,
    val nameVN: String? = null
)

@Serializable
data class TopPrediction(
    val name: String,
    val nameVN: String? = null,
    val confidence: Double
)

