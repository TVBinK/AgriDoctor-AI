package com.baothanhbin.core.data.impl

import android.util.Log
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.model.ChatConversationPayload
import com.baothanhbin.core.model.ChatHistoryMessagePayload
import com.baothanhbin.core.network.NetworkDataSource
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class ChatSyncRepository @Inject constructor(
    private val database: AgriDoctorDatabase,
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "ChatSyncRepository"
    }

    private val chatDao get() = database.chatDao()
    private val syncMutex = Mutex()

    suspend fun getAllChats(): List<ChatEntity> {
        return syncMutex.withLock {
            val ownerUserId = authRepository.getCurrentUserId().orEmpty()
            if (ownerUserId.isNotBlank()) {
                chatDao.assignAnonymousChats(ownerUserId)
            }
            chatDao.getAllChats(ownerUserId)
        }
    }

    suspend fun getChatById(chatId: Long): ChatEntity? {
        return chatDao.getChatById(chatId)
    }

    suspend fun saveChat(chat: ChatEntity): ChatEntity {
        return syncMutex.withLock {
            val existingChat = chat.id
                .takeIf { it > 0L }
                ?.let { chatDao.getChatById(it) }
            val currentUserId = authRepository.getCurrentUserId().orEmpty()
            val normalizedOwnerUserId = chat.ownerUserId.ifBlank {
                existingChat?.ownerUserId.orEmpty().ifBlank { currentUserId }
            }

            val preparedChat = chat.copy(
                id = existingChat?.id ?: chat.id,
                ownerUserId = normalizedOwnerUserId,
                serverChatId = chat.serverChatId ?: existingChat?.serverChatId,
                needsSync = true,
                updatedAt = chat.updatedAt.takeIf { it > 0L } ?: System.currentTimeMillis()
            )

            val localId = if (existingChat != null) {
                chatDao.updateChat(preparedChat.copy(id = existingChat.id))
                existingChat.id
            } else {
                chatDao.insertChat(preparedChat)
            }

            val localChat = chatDao.getChatById(localId) ?: preparedChat.copy(id = localId)
            val token = authRepository.getToken()
                ?.takeIf { it.isNotBlank() }

            if (token != null && localChat.ownerUserId.isNotBlank()) {
                syncPendingChatsLocked(localChat.ownerUserId, token)
            }

            chatDao.getChatById(localId) ?: localChat
        }
    }

    suspend fun deleteChat(chatId: Long): Result<Unit> {
        return try {
            syncMutex.withLock {
                val chat = chatDao.getChatById(chatId) ?: return@withLock
                val serverChatId = chat.serverChatId
                if (!serverChatId.isNullOrBlank()) {
                    val token = authRepository.getToken()
                        ?.takeIf { it.isNotBlank() }
                        ?: throw IllegalStateException(
                            "Khong the xoa lich su tro chuyen tren server vi ban chua dang nhap."
                        )
                    NetworkDataSource.deleteChatHistory(serverChatId, token).getOrThrow()
                }
                chatDao.deleteChat(chatId)
            }
            Result.success(Unit)
        } catch (error: Exception) {
            Result.failure(error)
        }
    }

    suspend fun syncFromServer() {
        syncMutex.withLock {
            val token = authRepository.getToken()
                ?.takeIf { it.isNotBlank() }
                ?: return
            val currentUserId = ensureCurrentUserId() ?: return

            chatDao.assignAnonymousChats(currentUserId)
            syncPendingChatsLocked(currentUserId, token)

            val response = NetworkDataSource.getChatHistory(token)
                ?.takeIf { it.success }
                ?: return

            response.data
                .sortedBy { it.updatedAt }
                .forEach { conversation ->
                    upsertServerConversation(
                        ownerUserId = currentUserId,
                        conversation = conversation
                    )
                }
        }
    }

    private suspend fun ensureCurrentUserId(): String? {
        authRepository.getCurrentUserId()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }

        val profile = authRepository.getProfile().getOrNull() ?: return null
        authRepository.saveUserId(profile.userId)
        return profile.userId
    }

    private suspend fun syncPendingChatsLocked(
        ownerUserId: String,
        token: String
    ) {
        val pendingChats = chatDao.getChatsNeedingSync(ownerUserId)
        pendingChats.forEach { localChat ->
            val messages = localChat.messages.map { it.toPayload() }
            val serverChatId = localChat.serverChatId
            val syncResult = if (serverChatId.isNullOrBlank()) {
                NetworkDataSource.createChatHistory(
                    title = localChat.title,
                    messages = messages,
                    token = token
                )
            } else {
                NetworkDataSource.updateChatHistory(
                    chatId = serverChatId,
                    title = localChat.title,
                    messages = messages,
                    token = token
                )
            }

            syncResult.onSuccess { syncedConversation ->
                upsertServerConversation(
                    ownerUserId = ownerUserId,
                    conversation = syncedConversation,
                    preferredLocalId = localChat.id
                )
            }.onFailure { error ->
                Log.w(TAG, "Failed to sync chat ${localChat.id}", error)
            }
        }
    }

    private suspend fun upsertServerConversation(
        ownerUserId: String,
        conversation: ChatConversationPayload,
        preferredLocalId: Long? = null
    ) {
        val existingChat = preferredLocalId
            ?.let { chatDao.getChatById(it) }
            ?: chatDao.getChatByServerChatId(conversation.id)

        val previousMessagesById = existingChat
            ?.messages
            ?.associateBy { it.id }
            .orEmpty()

        val mergedMessages = conversation.messages.map { message ->
            val previousMessage = previousMessagesById[message.messageId]
            ChatMessageData(
                id = message.messageId,
                text = message.text,
                isUser = message.isUser,
                timestamp = message.timestamp,
                imageUri = previousMessage?.imageUri?.takeIf { message.hasImage },
                hasImage = message.hasImage
            )
        }

        val localChat = ChatEntity(
            id = existingChat?.id ?: 0L,
            ownerUserId = ownerUserId,
            serverChatId = conversation.id,
            needsSync = false,
            title = conversation.title,
            messages = mergedMessages,
            updatedAt = conversation.updatedAt
        )

        if (existingChat != null) {
            chatDao.updateChat(localChat.copy(id = existingChat.id))
        } else {
            chatDao.insertChat(localChat)
        }
    }

    private fun ChatMessageData.toPayload(): ChatHistoryMessagePayload {
        return ChatHistoryMessagePayload(
            messageId = id,
            text = text,
            isUser = isUser,
            timestamp = timestamp,
            hasImage = hasImage || imageUri != null
        )
    }
}
