package com.baothanhbin.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeminiRequest(
    val contents: List<GeminiContent>
)

@Serializable
data class GeminiContent(
    val parts: List<GeminiPart>
)

@Serializable
data class GeminiPart(
    val text: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null
)

