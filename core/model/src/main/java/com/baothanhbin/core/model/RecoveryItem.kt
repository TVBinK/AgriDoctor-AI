package com.baothanhbin.core.model

data class RecoveryItem(
    val title: String,
    val steps: List<String>,
    val linkText: String? = null
)