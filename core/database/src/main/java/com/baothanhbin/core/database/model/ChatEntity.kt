package com.baothanhbin.core.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.converter.ChatMessageListConverter

@Entity(
    tableName = "chats",
    indices = [
        Index(value = ["ownerUserId"]),
        Index(value = ["serverChatId"], unique = true)
    ]
)
data class ChatEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val ownerUserId: String = "",
    val serverChatId: String? = null,
    val needsSync: Boolean = false,
    val title: String,
    @TypeConverters(ChatMessageListConverter::class)
    val messages: List<ChatMessageData>,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun getPreviewText(): String {
        val latestMessage = messages.lastOrNull() ?: return ""
        if (latestMessage.text.isNotBlank()) {
            return latestMessage.text.take(50)
        }
        return if (latestMessage.hasImage || latestMessage.imageUri != null) {
            "Da gui hinh anh."
        } else {
            ""
        }
    }
}
