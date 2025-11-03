package com.baothanhbin.feature.chatbot

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.network.SecretProvider
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.pow

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
    application: Application,
    private val database: AgriDoctorDatabase,
    private val secretProvider: SecretProvider
) : AndroidViewModel(application) {
    
    private val chatDao get() = database.chatDao()

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
    }

    private val MODEL_NAME =
        if (BuildConfig.BUILD_TYPE == "debug") secretProvider.getDebugModelName() else secretProvider.getReleaseModelName()

    private val API_KEY = secretProvider.getGeminiApiKey()

    // Sử dụng lazy initialization với try-catch để tránh lỗi dependency
    private val generativeModel by lazy {
        try {
            GenerativeModel(
                modelName = MODEL_NAME,
                apiKey = API_KEY
            )
        } catch (e: Exception) {
            Log.e("ChatbotViewModel", "Failed to initialize GenerativeModel: ${e.message}")
            null
        }
    }


    fun onMessageSent(message: String) {
        Log.d("ChatbotViewModel", "onMessageSent called with message: '$message'")
        if (message.isBlank()) {
            Log.d("ChatbotViewModel", "Message is blank, returning")
            return
        }

        viewModelScope.launch {
            Log.d("ChatbotViewModel", "Processing message in coroutine")
            // Add user message
            val userMessage = ChatMessage(
                id = "user_${System.currentTimeMillis()}",
                text = message,
                isUser = true
            )
            
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(userMessage)
            Log.d("ChatbotViewModel", "Added user message. Total messages: ${currentMessages.size}")
            
            _uiState.value = _uiState.value.copy(
                messages = currentMessages.toList(), // Convert to immutable list
                inputText = "",
                showWelcomeScreen = false,
                isLoading = true
            )
            Log.d("ChatbotViewModel", "Updated state with user message. Messages count: ${_uiState.value.messages.size}")
            Log.d("ChatbotViewModel", "Messages list: ${_uiState.value.messages.map { "${it.id}:${it.text.take(20)}" }}")

            // Call Gemini API to get bot response using GenerativeModel with retry
            Log.d("ChatbotViewModel", "Calling Gemini API for response with message: '$message'")
            val botResponse = try {
                val model = generativeModel
                if (model != null) {
                    withContext(Dispatchers.IO) {
                        generateContentWithRetry(model, message)
                    }
                } else {
                    Log.e("ChatbotViewModel", "GenerativeModel is null, cannot generate response")
                    "Sorry, I couldn't process your request. The AI service is not available."
                }
            } catch (e: Exception) {
                Log.e("ChatbotViewModel", "Exception when calling GenerativeModel: ${e.message}", e)
                getErrorMessage(e)
            }
            Log.d("ChatbotViewModel", "Received bot response: ${botResponse?.take(100)}")
            
            val botMessage = ChatMessage(
                id = "bot_${System.currentTimeMillis()}",
                text = botResponse ?: "Sorry, I couldn't process your request. Please try again.",
                isUser = false
            )

            currentMessages.add(botMessage)
            _uiState.value = _uiState.value.copy(
                messages = currentMessages.toList(), // Convert to immutable list
                isLoading = false
            )
            Log.d("ChatbotViewModel", "Added bot message. Total messages: ${currentMessages.size}")
            Log.d("ChatbotViewModel", "Messages list: ${currentMessages.map { "${it.id}:${it.text.take(20)}" }}")
            
            // Save chat to Room database
            saveCurrentChatToDatabase(currentMessages)
        }
    }

    private suspend fun saveCurrentChatToDatabase(messages: List<ChatMessage>) {
        withContext(Dispatchers.IO) {
            try {
                val currentChatId = _uiState.value.currentChatId
                val title = if (messages.isNotEmpty()) {
                    messages.first().text.take(50).ifEmpty { "New Chat" }
                } else {
                    "New Chat"
                }
                
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
                
                // Update current chat ID in state
                _uiState.value = _uiState.value.copy(currentChatId = savedId)
                loadChatHistory()
            } catch (e: Exception) {
                Log.e("ChatbotViewModel", "Error saving chat to database: ${e.message}", e)
            }
        }
    }

    fun onInputTextChanged(text: String) {
        Log.d("ChatbotViewModel", "onInputTextChanged called with text: '$text'")
        _uiState.value = _uiState.value.copy(inputText = text)
        Log.d("ChatbotViewModel", "Updated inputText in state: '${_uiState.value.inputText}'")
    }

    fun onSuggestedPromptClicked(prompt: String) {
        Log.d("ChatbotViewModel", "onSuggestedPromptClicked: '$prompt'")
        onMessageSent(prompt)
    }

    fun onClose() {
        Log.d("ChatbotViewModel", "onClose called")
        viewModelScope.launch {
            _uiState.value = ChatbotUiState()
        }
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
                } catch (e: Exception) {
                    Log.e("ChatbotViewModel", "Error loading chat history: ${e.message}", e)
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
                        val messages = chat.messages.map { it.toChatMessage() }
                        _uiState.value = _uiState.value.copy(
                            messages = messages,
                            currentChatId = chat.id,
                            showWelcomeScreen = false,
                            isDrawerOpen = false
                        )
                    }
                } catch (e: Exception) {
                    Log.e("ChatbotViewModel", "Error loading chat: ${e.message}", e)
                }
            }
        }
    }

    fun createNewChat() {
        viewModelScope.launch {
            // Save current chat before creating new one
            if (_uiState.value.messages.isNotEmpty()) {
                saveCurrentChatToDatabase(_uiState.value.messages)
            }
            
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
                    // If deleting current chat, reset to new chat
                    if (_uiState.value.currentChatId == chatId) {
                        _uiState.value = _uiState.value.copy(
                            messages = emptyList(),
                            currentChatId = null,
                            showWelcomeScreen = true
                        )
                    }
                    loadChatHistory()
                } catch (e: Exception) {
                    Log.e("ChatbotViewModel", "Error deleting chat: ${e.message}", e)
                }
            }
        }
    }
    
    /**
     * Generate content with retry logic for 503 and 429 errors
     */
    private suspend fun generateContentWithRetry(model: GenerativeModel, message: String): String {
        val maxRetries = 3
        var retryDelay = 1000L // Start with 1 second
        
        repeat(maxRetries) { attempt ->
            try {
                Log.d("ChatbotViewModel", "Attempt ${attempt + 1}/$maxRetries to generate content")
                val response = model.generateContent(message)
                val text = response.text ?: "Sorry, I couldn't generate a response."
                Log.d("ChatbotViewModel", "Successfully generated content")
                return text
            } catch (e: Exception) {
                Log.w("ChatbotViewModel", "Attempt ${attempt + 1} failed: ${e.message}")
                
                // Check if it's a retryable error
                if (isRetryableError(e) && attempt < maxRetries - 1) {
                    Log.d("ChatbotViewModel", "Retrying in ${retryDelay}ms...")
                    delay(retryDelay)
                    retryDelay = (retryDelay * 2).coerceAtMost(10000L) // Exponential backoff, max 10s
                } else {
                    // Last attempt failed or non-retryable error
                    throw e
                }
            }
        }
        
        // This shouldn't be reached, but just in case
        throw Exception("Failed to generate content after $maxRetries attempts")
    }
    
    /**
     * Check if an error is retryable (503, 429, network errors)
     */
    private fun isRetryableError(exception: Exception): Boolean {
        val message = exception.message ?: return false
        
        // Check for specific Gemini API error codes
        return when {
            message.contains("503", ignoreCase = true) -> true // Service Unavailable
            message.contains("429", ignoreCase = true) -> true // Too Many Requests
            message.contains("overloaded", ignoreCase = true) -> true
            message.contains("UNAVAILABLE", ignoreCase = true) -> true
            message.contains("DEADLINE_EXCEEDED", ignoreCase = true) -> true
            message.contains("Network", ignoreCase = true) -> true
            message.contains("timeout", ignoreCase = true) -> true
            else -> false
        }
    }
    
    /**
     * Get user-friendly error message
     */
    private fun getErrorMessage(exception: Exception): String {
        val message = exception.message ?: "Unknown error"
        
        return when {
            message.contains("503", ignoreCase = true) || 
            message.contains("overloaded", ignoreCase = true) || 
            message.contains("UNAVAILABLE", ignoreCase = true) -> 
                "Server đang quá tải. Vui lòng thử lại sau ít phút."
            
            message.contains("429", ignoreCase = true) -> 
                "Quá nhiều yêu cầu. Vui lòng đợi một chút rồi thử lại."
            
            message.contains("401", ignoreCase = true) || 
            message.contains("403", ignoreCase = true) -> 
                "Lỗi xác thực API. Vui lòng kiểm tra cấu hình."
            
            message.contains("Network", ignoreCase = true) || 
            message.contains("timeout", ignoreCase = true) -> 
                "Lỗi kết nối mạng. Vui lòng kiểm tra kết nối internet."
            
            message.contains("BLOCKED", ignoreCase = true) -> 
                "Nội dung bị chặn bởi chính sách an toàn."
            
            else -> "Xin lỗi, đã xảy ra lỗi: $message"
        }
    }
}

