package com.baothanhbin.core.model

data class TreatmentItem(
    val title: String,
    val subtitle: String? = null,
    val steps: List<String>,
    val linkText: String? = null
)