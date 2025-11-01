package com.baothanhbin.core.database.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.baothanhbin.core.database.converter.ChatMessageListConverter
import com.baothanhbin.core.database.converter.ChatMessageData

/**
 * Room entity for caching chat conversations
 */
@Entity(tableName = "chats")
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String, // Title của cuộc chat (có thể là message đầu tiên hoặc "New Chat")
    @TypeConverters(ChatMessageListConverter::class)
    val messages: List<ChatMessageData>,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getPreviewText(): String {
        return messages.lastOrNull()?.text?.take(50) ?: ""
    }
}

