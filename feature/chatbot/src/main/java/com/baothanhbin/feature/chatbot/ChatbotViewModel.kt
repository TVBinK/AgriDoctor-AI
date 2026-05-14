package com.baothanhbin.feature.chatbot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.model.ChatbotHistoryMessage
import com.baothanhbin.core.network.NetworkDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChatMessage(
    val id: String,
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

fun ChatMessage.toChatMessageData(): ChatMessageData {
    return ChatMessageData(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp
    )
}

fun ChatMessageData.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp
    )
}

data class ChatHistoryItem(
    val id: Long,
    val title: String,
    val preview: String,
    val updatedAt: Long
)

data class ChatbotUiState(
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val showWelcomeScreen: Boolean = true,
    val currentChatId: Long? = null,
    val chatHistory: List<ChatHistoryItem> = emptyList(),
    val isDrawerOpen: Boolean = false
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val database: AgriDoctorDatabase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val chatDao get() = database.chatDao()

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private val processedInitialMessages = mutableSetOf<String>()

    init {
        loadChatHistory()
    }

    fun onMessageSent(message: String) {
        val trimmedMessage = message.trim()
        if (trimmedMessage.isEmpty()) {
            return
        }

        viewModelScope.launch {
            val previousMessages = _uiState.value.messages
            val userMessage = ChatMessage(
                id = "user_${System.currentTimeMillis()}",
                text = trimmedMessage,
                isUser = true
            )
            val currentMessages = previousMessages + userMessage

            _uiState.value = _uiState.value.copy(
                messages = currentMessages,
                inputText = "",
                showWelcomeScreen = false,
                isLoading = true
            )

            val chatHistory = previousMessages
                .takeLast(10)
                .map { item ->
                    ChatbotHistoryMessage(
                        text = item.text,
                        isUser = item.isUser
                    )
                }

            val botResponse = withContext(Dispatchers.IO) {
                val token = authRepository.getToken()
                if (token.isNullOrBlank()) {
                    "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại để tiếp tục trò chuyện."
                } else {
                    NetworkDataSource.sendChatMessage(
                        message = trimmedMessage,
                        history = chatHistory,
                        token = token
                    ).getOrElse { error ->
                        error.message
                            ?: "Trợ lý cây trồng đang tạm gián đoạn. Vui lòng thử lại sau."
                    }
                }
            }

            val finalMessages = currentMessages + ChatMessage(
                id = "bot_${System.currentTimeMillis()}",
                text = botResponse,
                isUser = false
            )

            _uiState.value = _uiState.value.copy(
                messages = finalMessages,
                isLoading = false
            )

            saveCurrentChatToDatabase(finalMessages)
        }
    }

    private suspend fun saveCurrentChatToDatabase(messages: List<ChatMessage>) {
        withContext(Dispatchers.IO) {
            try {
                val currentChatId = _uiState.value.currentChatId
                val title = messages.firstOrNull()?.text?.take(50)?.ifEmpty { "New Chat" } ?: "New Chat"

                val chatEntity = ChatEntity(
                    id = currentChatId ?: 0,
                    title = title,
                    messages = messages.map { it.toChatMessageData() },
                    updatedAt = System.currentTimeMillis()
                )

                val savedId = if (currentChatId != null) {
                    chatDao.updateChat(chatEntity)
                    currentChatId
                } else {
                    chatDao.insertChat(chatEntity)
                }

                _uiState.value = _uiState.value.copy(currentChatId = savedId)
                loadChatHistory()
            } catch (error: Exception) {
                Log.e("ChatbotViewModel", "Failed to save chat", error)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun processInitialMessageIfNeeded(message: String?): Boolean {
        if (message.isNullOrBlank()) {
            return false
        }

        if (processedInitialMessages.contains(message)) {
            return false
        }

        processedInitialMessages.add(message)
        onMessageSent(message)
        return true
    }

    fun loadChatHistory() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val chats = chatDao.getAllChats()
                    val historyItems = chats.map { chat ->
                        ChatHistoryItem(
                            id = chat.id,
                            title = chat.title,
                            preview = chat.getPreviewText(),
                            updatedAt = chat.updatedAt
                        )
                    }
                    _uiState.value = _uiState.value.copy(chatHistory = historyItems)
                } catch (error: Exception) {
                    Log.e("ChatbotViewModel", "Failed to load chat history", error)
                }
            }
        }
    }

    fun loadChat(chatId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val chat = chatDao.getChatById(chatId)
                    if (chat != null) {
                        processedInitialMessages.clear()
                        _uiState.value = _uiState.value.copy(
                            messages = chat.messages.map { it.toChatMessage() },
                            currentChatId = chat.id,
                            showWelcomeScreen = false,
                            isDrawerOpen = false
                        )
                    }
                } catch (error: Exception) {
                    Log.e("ChatbotViewModel", "Failed to load chat", error)
                }
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            if (_uiState.value.messages.isNotEmpty()) {
                saveCurrentChatToDatabase(_uiState.value.messages)
            }

            processedInitialMessages.clear()
            _uiState.value = _uiState.value.copy(
                messages = emptyList(),
                currentChatId = null,
                inputText = "",
                showWelcomeScreen = true,
                isDrawerOpen = false
            )
            loadChatHistory()
        }
    }

    fun toggleDrawer() {
        _uiState.value = _uiState.value.copy(
            isDrawerOpen = !_uiState.value.isDrawerOpen
        )
        if (_uiState.value.isDrawerOpen) {
            loadChatHistory()
        }
    }

    fun closeDrawer() {
        _uiState.value = _uiState.value.copy(isDrawerOpen = false)
    }

    fun deleteChat(chatId: Long) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    chatDao.deleteChat(chatId)
                    if (_uiState.value.currentChatId == chatId) {
                        _uiState.value = _uiState.value.copy(
                            messages = emptyList(),
                            currentChatId = null,
                            showWelcomeScreen = true
                        )
                    }
                    loadChatHistory()
                } catch (error: Exception) {
                    Log.e("ChatbotViewModel", "Failed to delete chat", error)
                }
            }
        }
    }
}
