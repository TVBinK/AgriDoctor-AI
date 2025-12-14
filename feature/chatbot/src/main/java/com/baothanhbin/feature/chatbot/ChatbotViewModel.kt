package com.baothanhbin.feature.chatbot

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.baothanhbin.core.database.AgriDoctorDatabase
import com.baothanhbin.core.database.converter.ChatMessageData
import com.baothanhbin.core.database.model.ChatEntity
import com.baothanhbin.core.database.model.DiseaseListEntity
import com.baothanhbin.core.data.repository.ApiKeyRepository
import com.baothanhbin.core.network.NetworkDataSource
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
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
    private var isInitializingDiseaseCache = false
    
    // Track processed initial messages to prevent re-processing
    private val processedInitialMessages = mutableSetOf<String>()

    init {
        loadChatHistory()

        // Lần đầu mở chatbot: chuẩn bị sẵn API key và cache danh sách bệnh cây
        viewModelScope.launch(Dispatchers.IO) {
            try {
                getOrInitializeGenerativeModel()
                ensureDiseaseCacheLoaded()
            } catch (e: Exception) {
                Log.e("ChatbotViewModel", "Error initializing chatbot data: ${e.message}", e)
            }
        }
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
                    apiKey = apiKey,
                    systemInstruction = content {
                        text(
                            """
                            Bạn là bác sĩ cây trồng (chuyên gia bệnh cây) trong ứng dụng AgriDoctorAI.
                            Nhiệm vụ của bạn:
                            - Chỉ tập trung vào các chủ đề liên quan đến cây trồng, nông nghiệp, sâu bệnh, dinh dưỡng và chăm sóc cây.
                            - Khi người dùng hỏi một câu hỏi chung chung hoặc không liên quan đến cây trồng, hãy nhẹ nhàng hướng lại chủ đề bằng cách gợi ý họ mô tả:
                              + Loại cây trồng
                              + Triệu chứng, biểu hiện bất thường
                              + Điều kiện đất, nước, thời tiết, phân bón đã sử dụng
                            - Trả lời **bằng tiếng Việt**, rõ ràng, dễ hiểu, ưu tiên nông dân Việt Nam.
                            - Với mỗi câu hỏi về bệnh cây, cố gắng trình bày theo cấu trúc:
                              1) Khả năng bệnh / vấn đề chính
                              2) Giải thích ngắn gọn
                              3) Hướng xử lý khuyến nghị
                              4) Cách phòng ngừa trong tương lai
                            - Nếu thông tin người dùng cung cấp chưa đủ để chẩn đoán chính xác, hãy nói rõ điều đó và hỏi thêm các thông tin cần thiết.
                            - Không trả lời các chủ đề nhạy cảm, chính trị, tôn giáo, hoặc ngoài phạm vi sức khỏe cây trồng.
                            """.trimIndent()
                        )
                    }
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

    /**
     * Tóm tắt JSON danh sách bệnh thành chuỗi RẤT ngắn gọn:
     * - Chỉ lấy một vài bệnh đầu tiên.
     * - Mỗi bệnh: tên + code + 1–2 keyword từ symptoms.
     * - Giới hạn tổng độ dài nhỏ để giảm token tối đa.
     */
    private fun buildDiseaseSummary(diseasesJson: String): String {
        return try {
            // Regex đơn giản bắt "code": "..." và "diseaseName": "..."
            val codeRegex = """"code"\s*:\s*"([^"]+)"""".toRegex()
            val nameRegex = """"diseaseName"\s*:\s*"([^"]+)"""".toRegex()
            val symptomsRegex = """"symptoms"\s*:\s*"([^"]+)"""".toRegex()

            val codes = codeRegex.findAll(diseasesJson).toList()
            val names = nameRegex.findAll(diseasesJson).toList()
            val symptoms = symptomsRegex.findAll(diseasesJson).toList()

            if (names.isEmpty()) {
                return diseasesJson.take(500)
            }

            val count = minOf(names.size, codes.size, symptoms.size, 5) // chỉ lấy tối đa 5 bệnh

            val summaryLines = (0 until count).map { index ->
                val code = codes.getOrNull(index)?.groupValues?.getOrNull(1) ?: "N/A"
                val name = names.getOrNull(index)?.groupValues?.getOrNull(1) ?: "Unknown"
                val symptomText = symptoms.getOrNull(index)?.groupValues?.getOrNull(1) ?: ""
                // Lấy rất ngắn: chỉ vài keyword đầu tiên từ symptoms
                val shortSymptoms = symptomText
                    .replace("\\n", " ")
                    .replace("\n", " ")
                    .split(Regex("[,.]"))
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .take(2) // tối đa 2 mảnh mô tả
                    .joinToString(separator = ", ")
                    .take(60)

                "- $name ($code): triệu chứng chính: $shortSymptoms"
            }

            // Tổng độ dài summary giới hạn rất thấp
            summaryLines.joinToString(separator = "\n").take(400)
        } catch (e: Exception) {
            Log.e("ChatbotViewModel", "Error summarizing diseasesJson: ${e.message}", e)
            diseasesJson.take(300)
        }
    }

    /**
     * Đảm bảo đã có cache danh sách bệnh cây trong Room.
     * Nếu chưa có thì gọi API /api/diseases và lưu raw JSON vào bảng diseases_cache.
     */
    private suspend fun ensureDiseaseCacheLoaded() {
        if (isInitializingDiseaseCache) return

        isInitializingDiseaseCache = true
        try {
            val diseaseDao = database.diseaseDao()

            // Nếu đã có cache thì không cần gọi API lại
            val existingCache = withContext(Dispatchers.IO) {
                diseaseDao.getLatestDiseaseList()
            }
            if (existingCache != null && existingCache.json.isNotBlank()) {
                Log.d("ChatbotViewModel", "Disease cache already exists, skip fetching")
                return
            }

            Log.d("ChatbotViewModel", "Fetching diseases list from server")
            val diseasesJson = NetworkDataSource.getDiseasesJson()

            if (diseasesJson != null && diseasesJson.isNotBlank()) {
                withContext(Dispatchers.IO) {
                    diseaseDao.upsertDiseaseList(
                        DiseaseListEntity(
                            id = 0,
                            json = diseasesJson,
                            updatedAt = System.currentTimeMillis()
                        )
                    )
                }
                Log.d("ChatbotViewModel", "Disease cache saved to database")
            } else {
                Log.e("ChatbotViewModel", "Failed to fetch diseases list from server")
            }
        } catch (e: Exception) {
            Log.e("ChatbotViewModel", "Error loading disease cache: ${e.message}", e)
        } finally {
            isInitializingDiseaseCache = false
        }
    }


    fun onMessageSent(message: String) {
        viewModelScope.launch {
            // Add user message
            val userMessage = ChatMessage(
                id = "user_${System.currentTimeMillis()}",
                text = message,
                isUser = true
            )
            
            val currentMessages = _uiState.value.messages.toMutableList()
            currentMessages.add(userMessage)
            
            _uiState.value = _uiState.value.copy(
                messages = currentMessages.toList(), // Convert to immutable list
                inputText = "",
                showWelcomeScreen = false,
                isLoading = true
            )

            // Call Gemini API to get bot response using GenerativeModel
            val botResponse = withContext(Dispatchers.Default) {
                // Lấy hoặc khởi tạo GenerativeModel với API key
                val model = withContext(Dispatchers.IO) {
                    getOrInitializeGenerativeModel()
                }

                if (model != null) {
                    // Đảm bảo đã có cache danh sách bệnh (nếu chưa có thì fetch)
                    ensureDiseaseCacheLoaded()

                    // Lấy danh sách bệnh từ Room (raw JSON) để làm ngữ cảnh cho Gemini
                    val diseasesJson = withContext(Dispatchers.IO) {
                        try {
                            database.diseaseDao().getLatestDiseaseList()?.json
                        } catch (e: Exception) {
                            null
                        }
                    }

                    withContext(Dispatchers.IO) {
                        // Rút gọn / tóm tắt JSON bệnh cây để tránh gửi quá dài mỗi lần gọi
                        val response = if (diseasesJson.isNullOrBlank()) {
                            // Nếu chưa có dữ liệu bệnh, fallback về gọi như cũ
                            model.generateContent(message)
                        } else {
                            val summary = buildDiseaseSummary(diseasesJson)

                            // Build disease context prompt
                            val diseaseContextPrompt = """
                                Đây là danh sách bệnh cây trồng đã được tóm tắt từ hệ thống backend AgriDoctorAI.
                                Hãy dùng thông tin tóm tắt này như một cơ sở tham chiếu khi tư vấn bệnh cây cho người dùng:
                                
                                $summary
                                """.trimIndent()

                            // Log toàn bộ nội dung gửi lên Gemini
                            val fullPromptLog = """
                                === [GEMINI PROMPT SENT] ===
                                [SYSTEM CONTEXT]:
                                $diseaseContextPrompt
                                
                                [USER MESSAGE]:
                                $message
                                ============================
                            """.trimIndent()
                            Log.d("ChatbotViewModel", fullPromptLog)

                            // Truyền thêm danh sách bệnh (đã tóm tắt) để Gemini tham chiếu khi trả lời
                            model.generateContent(
                                content {
                                    role = "user"
                                    text(diseaseContextPrompt)
                                },
                                content {
                                    role = "user"
                                    text(message)
                                }
                            )
                        }

                        // Loại bỏ toàn bộ ký tự '*' trong câu trả lời để tránh markdown bullet
                        val rawText = response.text ?: "Sorry, I couldn't generate a response."
                        
                        Log.d("ChatbotViewModel", """
                            === [GEMINI RESPONSE] ===
                            $rawText
                            =========================
                        """.trimIndent())
                        
                        rawText.replace("*", "")
                    }
                } else {
                    "Sorry, I couldn't process your request. The AI service is not available. Please check your connection and try again."
                }
            }

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
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    /**
     * Xử lý initialMessage chỉ một lần để tránh gửi lại khi navigate qua lại giữa các tab
     * @return true nếu message đã được xử lý, false nếu đã được xử lý trước đó
     */
    fun processInitialMessageIfNeeded(message: String?): Boolean {
        if (message.isNullOrBlank()) {
            return false
        }
        
        // Kiểm tra xem message này đã được xử lý chưa
        if (processedInitialMessages.contains(message)) {
            return false
        }
        
        // Đánh dấu đã xử lý và gửi message
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
                        // Clear processed initial messages when loading a different chat
                        // This prevents issues when switching between chats
                        processedInitialMessages.clear()
                        
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
            
            // Clear processed initial messages when creating new chat
            // This allows users to ask the same question again if they navigate from DiagnoseResultScreen
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

