package com.baothanhbin.core.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatHistoryMessagePayload(
    val messageId: String,
    val text: String = "",
    val isUser: Boolean,
    val timestamp: Long,
    val hasImage: Boolean = false
)

@Serializable
data class ChatHistoryUpsertRequest(
    val title: String,
    val messages: List<ChatHistoryMessagePayload> = emptyList()
)

@Serializable
data class ChatConversationPayload(
    val id: String,
    val userId: String,
    val title: String,
    val updatedAt: Long,
    val createdAt: Long,
    val messages: List<ChatHistoryMessagePayload> = emptyList()
)

@Serializable
data class ChatHistoryListResponse(
    val success: Boolean,
    val data: List<ChatConversationPayload> = emptyList()
)

@Serializable
data class ChatHistoryItemResponse(
    val success: Boolean,
    val data: ChatConversationPayload? = null
)
