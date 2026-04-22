package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

/**
 * API response model for disease diagnosis
 */
@Serializable
data class DiagnoseApiResponse(
    val success: Boolean,
    val data: DiagnoseData
)

@Serializable
data class DiagnoseData(
    val diseaseName: String,
    val possibleProblems: List<String>,
    val symptoms: String,
    val causes: String,
    val treatment: List<TreatmentData>,
    val recoveryCare: List<RecoveryCareData>,
    val detections: List<DetectionData> = emptyList()
)

@Serializable
data class DetectionData(
    val name: String,
    val confidence: Double,
    val box: List<Double>
)

@Serializable
data class TreatmentData(
    val title: String,
    val subtitle: String? = null,
    val steps: List<String>,
    val linkText: String? = null
)

@Serializable
data class RecoveryCareData(
    val title: String,
    val steps: List<String>,
    val linkText: String? = null
)
