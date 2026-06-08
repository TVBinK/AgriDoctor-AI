package com.baothanhbin.core.database.converter

import androidx.room.TypeConverter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

@Serializable
data class ChatMessageData(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val imageUri: String? = null,
    val hasImage: Boolean = false
)

class ChatMessageListConverter {
    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val chatMessageDataListSerializer = ListSerializer(kotlinx.serialization.serializer<ChatMessageData>())
    
    @TypeConverter
    fun fromString(value: String?): List<ChatMessageData> {
        if (value == null || value.isEmpty()) return emptyList()
        return try {
            json.decodeFromString(chatMessageDataListSerializer, value)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    @TypeConverter
    fun toString(list: List<ChatMessageData>): String {
        return json.encodeToString(chatMessageDataListSerializer, list)
    }
}

