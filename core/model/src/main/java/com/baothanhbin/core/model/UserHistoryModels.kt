package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

@Serializable
data class UserHistoryResponse(
    val success: Boolean,
    val data: List<UserHistoryItem> = emptyList()
)

@Serializable
data class UserHistoryItem(
    val id: String,
    val type: String,
    val timestamp: Long,
    val plantName: String? = null,
    val plantNameVN: String? = null,
    val confidence: Double? = null,
    val classificationStatus: String? = null,
    val icon: String? = null,
    val description: String? = null,
    val scientificName: String? = null,
    val family: String? = null,
    val commonNames: List<String> = emptyList(),
    val growingRegions: List<String> = emptyList(),
    val season: String? = null,
    val careTips: List<String> = emptyList(),
    val commonDiseases: List<String> = emptyList(),
    val diseaseName: String? = null,
    val possibleProblems: List<String> = emptyList(),
    val symptoms: String? = null,
    val causes: String? = null,
    val treatment: List<TreatmentData> = emptyList(),
    val recoveryCare: List<RecoveryCareData> = emptyList(),
    val detections: List<DetectionData> = emptyList(),
    val resultType: String? = null,
    val rejectedInput: Boolean = false
)
