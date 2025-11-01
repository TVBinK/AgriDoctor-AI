package com.baothanhbin.core.model

data class LoadedResult(
    val diseaseName: String,
    val possibleProblems: List<String>,
    val symptoms: String,
    val causes: String,
    val treatment: List<TreatmentItem>,
    val recoveryCare: List<RecoveryItem>
)