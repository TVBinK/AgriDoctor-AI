package com.baothanhbin.feature.chatbot

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.data.impl.ChatSyncRepository
import com.baothanhbin.core.data.repository.AuthRepository
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.model.ChatbotHistoryMessage
import com.baothanhbin.core.network.NetworkDataSource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
    val timestamp: Long = System.currentTimeMillis(),
    val imageUri: String? = null,
    val hasImage: Boolean = false
)

fun ChatMessage.toChatMessageData(): ChatMessageData {
    return ChatMessageData(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        imageUri = imageUri,
        hasImage = hasImage || imageUri != null
    )
}

fun ChatMessageData.toChatMessage(): ChatMessage {
    return ChatMessage(
        id = id,
        text = text,
        isUser = isUser,
        timestamp = timestamp,
        imageUri = imageUri,
        hasImage = hasImage || imageUri != null
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
    val isDrawerOpen: Boolean = false,
    val selectedImageUri: Uri? = null
)

@HiltViewModel
class ChatbotViewModel @Inject constructor(
    private val chatSyncRepository: ChatSyncRepository,
    private val authRepository: AuthRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private val processedInitialMessages = mutableSetOf<String>()

    init {
        loadChatHistory()
    }

    fun onMessageSent(message: String) {
        val trimmedMessage = message.trim()
        val selectedImageUri = _uiState.value.selectedImageUri
        if (trimmedMessage.isEmpty() && selectedImageUri == null) {
            return
        }

        viewModelScope.launch {
            val previousMessages = _uiState.value.messages
            val userMessage = ChatMessage(
                id = "user_${System.currentTimeMillis()}",
                text = trimmedMessage,
                isUser = true,
                imageUri = selectedImageUri?.toString(),
                hasImage = selectedImageUri != null
            )
            val currentMessages = previousMessages + userMessage

            _uiState.value = _uiState.value.copy(
                messages = currentMessages,
                inputText = "",
                selectedImageUri = null,
                showWelcomeScreen = false,
                isLoading = true
            )

            val chatHistory = previousMessages
                .takeLast(10)
                .mapNotNull { item ->
                    val historyText = item.toHistoryText()
                    if (historyText.isBlank()) {
                        null
                    } else {
                        ChatbotHistoryMessage(
                            text = historyText,
                            isUser = item.isUser
                        )
                    }
                }

            val botResponse = withContext(Dispatchers.IO) {
                val token = authRepository.getToken()
                if (token.isNullOrBlank()) {
                    "Phien dang nhap da het han. Vui long dang nhap lai de tiep tuc tro chuyen."
                } else {
                    val selectedImage = selectedImageUri?.let { readSelectedImage(it) }
                    if (selectedImageUri != null && selectedImage == null) {
                        "Khong the doc anh da chon. Vui long chon anh khac va thu lai."
                    } else {
                        NetworkDataSource.sendChatMessage(
                            message = trimmedMessage,
                            history = chatHistory,
                            token = token,
                            imageBytes = selectedImage?.bytes,
                            imageFileName = selectedImage?.fileName ?: "chatbot_image.jpg",
                            imageMimeType = selectedImage?.mimeType ?: "image/jpeg"
                        ).getOrElse { error ->
                            error.message
                                ?: "Tro ly cay trong dang tam gian doan. Vui long thu lai sau."
                        }
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
        try {
            val currentChatId = _uiState.value.currentChatId
            val title = messages.firstOrNull()?.text?.take(50)?.ifBlank { "New Chat" } ?: "New Chat"

            val savedChat = withContext(Dispatchers.IO) {
                chatSyncRepository.saveChat(
                    ChatEntity(
                        id = currentChatId ?: 0L,
                        title = title,
                        messages = messages.map { it.toChatMessageData() },
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }

            _uiState.value = _uiState.value.copy(currentChatId = savedChat.id)
            loadChatHistory()
        } catch (error: Exception) {
            Log.e("ChatbotViewModel", "Failed to save chat", error)
        }
    }

    fun onInputTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onImageSelected(uri: Uri?) {
        if (uri == null) {
            return
        }
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        _uiState.value = _uiState.value.copy(selectedImageUri = uri)
    }

    fun clearSelectedImage() {
        _uiState.value = _uiState.value.copy(selectedImageUri = null)
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
            try {
                val historyItems = withContext(Dispatchers.IO) {
                    chatSyncRepository.getAllChats().map { chat ->
                        ChatHistoryItem(
                            id = chat.id,
                            title = chat.title,
                            preview = chat.getPreviewText(),
                            updatedAt = chat.updatedAt
                        )
                    }
                }
                _uiState.value = _uiState.value.copy(chatHistory = historyItems)
            } catch (error: Exception) {
                Log.e("ChatbotViewModel", "Failed to load chat history", error)
            }
        }
    }

    fun loadChat(chatId: Long) {
        viewModelScope.launch {
            try {
                val chat = withContext(Dispatchers.IO) {
                    chatSyncRepository.getChatById(chatId)
                }
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
                selectedImageUri = null,
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
            try {
                val deleteResult = withContext(Dispatchers.IO) {
                    chatSyncRepository.deleteChat(chatId)
                }
                deleteResult.onSuccess {
                    if (_uiState.value.currentChatId == chatId) {
                        _uiState.value = _uiState.value.copy(
                            messages = emptyList(),
                            currentChatId = null,
                            showWelcomeScreen = true
                        )
                    }
                    loadChatHistory()
                }.onFailure { error ->
                    Log.e("ChatbotViewModel", "Failed to delete chat", error)
                }
            } catch (error: Exception) {
                Log.e("ChatbotViewModel", "Failed to delete chat", error)
            }
        }
    }

    private fun readSelectedImage(uri: Uri): SelectedChatImage? {
        return try {
            val bytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: return null

            SelectedChatImage(
                bytes = bytes,
                fileName = getDisplayName(uri) ?: "chatbot_image.jpg",
                mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
            )
        } catch (error: Exception) {
            Log.e("ChatbotViewModel", "Failed to read selected image", error)
            null
        }
    }

    private fun getDisplayName(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                cursor.getString(nameIndex)
            } else {
                null
            }
        }
    }
}

private data class SelectedChatImage(
    val bytes: ByteArray,
    val fileName: String,
    val mimeType: String
)

private fun ChatMessage.toHistoryText(): String {
    if (text.isNotBlank()) {
        return text
    }

    return if (hasImage || imageUri != null) {
        "Nguoi dung da gui mot anh."
    } else {
        ""
    }
}
