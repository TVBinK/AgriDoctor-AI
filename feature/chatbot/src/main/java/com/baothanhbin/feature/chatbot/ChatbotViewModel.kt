package com.baothanhbin.feature.chatbot

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.data.repository.ApiKeyRepository
import com.google.ai.client.generativeai.GenerativeModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
    private val apiKeyRepository: ApiKeyRepository
) : AndroidViewModel(application) {
    
    private val chatDao get() = database.chatDao()

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState: StateFlow<ChatbotUiState> = _uiState.asStateFlow()

    private var _generativeModel: GenerativeModel? = null
    private var isInitializingApiKey = false

    init {
        loadChatHistory()
    }

    // Model name cho Gemini API
    // SDK 0.9.0 tự động thêm prefix "models/" nên chỉ cần tên model không có prefix
    // Sử dụng gemini-2.5-flash - model nhanh và được hỗ trợ tốt trong API v1beta
    private val MODEL_NAME = "gemini-2.5-flash"

    /**
     * Khởi tạo GenerativeModel với API key từ repository
     * Lần đầu vào chat sẽ fetch API key từ server nếu chưa có trong cache
     */
    private suspend fun getOrInitializeGenerativeModel(): GenerativeModel? {
        if (_generativeModel != null) {
            return _generativeModel
        }

        // Tránh fetch nhiều lần đồng thời
        if (isInitializingApiKey) {
            return null
        }

        isInitializingApiKey = true
        try {
            // Lấy API key từ repository (từ cache hoặc từ server)
            val apiKey = apiKeyRepository.getApiKey()
            
            if (apiKey != null) {
                _generativeModel = GenerativeModel(
                    modelName = MODEL_NAME,
                    apiKey = apiKey
                )
                Log.d("ChatbotViewModel", "GenerativeModel initialized successfully")
            } else {
                Log.e("ChatbotViewModel", "Failed to get API key from repository")
            }
        } catch (e: Exception) {
            Log.e("ChatbotViewModel", "Failed to initialize GenerativeModel: ${e.message}", e)
        } finally {
            isInitializingApiKey = false
        }

        return _generativeModel
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

            // Call Gemini API to get bot response using GenerativeModel
            Log.d("ChatbotViewModel", "Calling Gemini API for response with message: '$message'")
            val botResponse = try {
                // Lấy hoặc khởi tạo GenerativeModel với API key
                val model = withContext(Dispatchers.IO) {
                    getOrInitializeGenerativeModel()
                }
                
                if (model != null) {
                    withContext(Dispatchers.IO) {
                        val response = model.generateContent(message)
                        response.text ?: "Sorry, I couldn't generate a response."
                    }
                } else {
                    Log.e("ChatbotViewModel", "GenerativeModel is null, cannot generate response")
                    "Sorry, I couldn't process your request. The AI service is not available. Please check your connection and try again."
                }
            } catch (e: Exception) {
                Log.e("ChatbotViewModel", "Exception when calling GenerativeModel: ${e.message}", e)
                
                // Xử lý các lỗi phổ biến với message rõ ràng hơn
                val errorMessage = when {
                    e.message?.contains("429") == true || 
                    e.message?.contains("quota") == true || 
                    e.message?.contains("RESOURCE_EXHAUSTED") == true -> {
                        "Đã vượt quá giới hạn sử dụng API. Vui lòng thử lại sau hoặc kiểm tra quota của bạn."
                    }
                    e.message?.contains("401") == true || 
                    e.message?.contains("403") == true -> {
                        "API key không hợp lệ hoặc không có quyền truy cập. Vui lòng kiểm tra cài đặt API key."
                    }
                    e.message?.contains("404") == true -> {
                        "Model không tìm thấy. Vui lòng kiểm tra cấu hình model name."
                    }
                    else -> {
                        "Xin lỗi, đã xảy ra lỗi: ${e.message ?: "Unknown error"}"
                    }
                }
                
                errorMessage
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
}

