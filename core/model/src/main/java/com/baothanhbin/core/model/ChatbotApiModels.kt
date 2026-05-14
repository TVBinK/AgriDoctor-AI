package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatbotHistoryMessage(
    val text: String,
    val isUser: Boolean
)

@Serializable
data class ChatbotRequest(
    val message: String,
    val history: List<ChatbotHistoryMessage> = emptyList()
)

@Serializable
data class ChatbotResponse(
    val success: Boolean,
    val data: ChatbotResponseData
)

@Serializable
data class ChatbotResponseData(
    val text: String
)
