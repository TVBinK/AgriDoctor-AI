package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

/**
 * Response model cho API key endpoint
 */
@Serializable
data class ApiKeyResponse(
    val success: Boolean,
    val data: ApiKeyData
)

@Serializable
data class ApiKeyData(
    val apiKey: String
)

