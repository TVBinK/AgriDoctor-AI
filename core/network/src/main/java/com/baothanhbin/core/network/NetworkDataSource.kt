package com.baothanhbin.core.network

import android.util.Log
import com.baothanhbin.core.model.ApiErrorResponse
import com.baothanhbin.core.model.ChatConversationPayload
import com.baothanhbin.core.model.ChatHistoryItemResponse
import com.baothanhbin.core.model.ChatHistoryListResponse
import com.baothanhbin.core.model.ChatHistoryMessagePayload
import com.baothanhbin.core.model.ChatHistoryUpsertRequest
import com.baothanhbin.core.model.ChatbotHistoryMessage
import com.baothanhbin.core.model.ChatbotRequest
import com.baothanhbin.core.model.ChatbotResponse
import com.baothanhbin.core.model.ClassifyApiResponse
import com.baothanhbin.core.model.DetectionData
import com.baothanhbin.core.model.DiagnoseApiResponse
import com.baothanhbin.core.model.DiagnoseData
import com.baothanhbin.core.model.RecoveryCareData
import com.baothanhbin.core.model.TreatmentData
import com.baothanhbin.core.model.UserHistoryResponse
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.net.SocketTimeoutException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object NetworkDataSource {
    private val networkJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    suspend fun detectImageTyped(
        imageBytes: ByteArray,
        fileName: String,
        mimeTypeString: String,
        token: String? = null
    ): DiagnoseApiResponse? = detectImageTyped(imageBytes, fileName, ContentType.parse(mimeTypeString), token)

    suspend fun detectImageTyped(
        imageBytes: ByteArray,
        fileName: String = "image.jpg",
        mimeType: ContentType = ContentType.Image.JPEG,
        token: String? = null
    ): DiagnoseApiResponse? = withContext(Dispatchers.IO) {
        try {
            val ensuredFileName = ensureFileName(fileName, mimeType)
            val response = NetworkClients.detectClient.submitFormWithBinaryData(
                formData = formData {
                    append(
                        key = "image",
                        value = imageBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, mimeType.toString())
                            append(HttpHeaders.ContentDisposition, "filename=\"$ensuredFileName\"")
                        }
                    )
                }
            ) {
                if (token != null) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                parseDetectResponse(response.bodyAsText())
            } else {
                null
            }
        } catch (e: HttpRequestTimeoutException) {
            Log.e("detectImageTyped", "Detect request timed out", e)
            null
        } catch (e: SocketTimeoutException) {
            Log.e("detectImageTyped", "Detect socket timed out", e)
            null
        } catch (e: Exception) {
            Log.e("detectImageTyped", "Detect request failed", e)
            null
        }
    }

    suspend fun sendChatMessage(
        message: String,
        history: List<ChatbotHistoryMessage>,
        token: String,
        imageBytes: ByteArray? = null,
        imageFileName: String = "chatbot_image.jpg",
        imageMimeType: String = "image/jpeg"
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = if (imageBytes != null) {
                val mimeType = ContentType.parse(imageMimeType)
                NetworkClients.chatbotClient.submitFormWithBinaryData(
                    formData = formData {
                        append("message", message)
                        append("history", networkJson.encodeToString(history))
                        append(
                            key = "image",
                            value = imageBytes,
                            headers = Headers.build {
                                append(HttpHeaders.ContentType, mimeType.toString())
                                append(HttpHeaders.ContentDisposition, "filename=\"${ensureFileName(imageFileName, mimeType)}\"")
                            }
                        )
                    }
                ) {
                    header(HttpHeaders.Authorization, "Bearer $token")
                }
            } else {
                NetworkClients.chatbotClient.post("") {
                    header(HttpHeaders.Authorization, "Bearer $token")
                    contentType(ContentType.Application.Json)
                    setBody(
                        ChatbotRequest(
                            message = message,
                            history = history
                        )
                    )
                }
            }

            if (response.status == HttpStatusCode.OK) {
                val payload: ChatbotResponse = response.body()
                val text = payload.data.text.trim()
                if (payload.success && text.isNotEmpty()) {
                    Result.success(text)
                } else {
                    Result.failure(IllegalStateException("Dịch vụ chatbot tạm thời không khả dụng."))
                }
            } else {
                Result.failure(response.toApiException())
            }
        } catch (e: Exception) {
            Log.e("sendChatMessage", "Chatbot request failed", e)
            Result.failure(IllegalStateException("Không thể kết nối đến trợ lý cây trồng."))
        }
    }

    suspend fun getDiseasesJson(): String? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.diseasesClient.get("")

            if (response.status == HttpStatusCode.OK) {
                response.bodyAsText()
            } else {
                Log.e("getDiseasesJson", "Failed with status: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("getDiseasesJson", "Error getting diseases", e)
            null
        }
    }

    suspend fun classifyImageTyped(
        imageBytes: ByteArray,
        fileName: String,
        mimeTypeString: String,
        token: String? = null
    ): ClassifyApiResponse? = classifyImageTyped(imageBytes, fileName, ContentType.parse(mimeTypeString), token)

    suspend fun classifyImageTyped(
        imageBytes: ByteArray,
        fileName: String = "image.jpg",
        mimeType: ContentType = ContentType.Image.JPEG,
        token: String? = null
    ): ClassifyApiResponse? = withContext(Dispatchers.IO) {
        try {
            val ensuredFileName = ensureFileName(fileName, mimeType)
            val response = NetworkClients.classifyClient.submitFormWithBinaryData(
                formData = formData {
                    append(
                        key = "image",
                        value = imageBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, mimeType.toString())
                            append(HttpHeaders.ContentDisposition, "filename=\"$ensuredFileName\"")
                        }
                    )
                }
            ) {
                if (token != null) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("classifyImageTyped", "Classify request failed", e)
            null
        }
    }

    suspend fun getUserHistory(token: String): UserHistoryResponse? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.historyClient.get("") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status == HttpStatusCode.OK) {
                response.body<UserHistoryResponse>()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("getUserHistory", "Failed to fetch user history", e)
            null
        }
    }

    suspend fun getChatHistory(token: String): ChatHistoryListResponse? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.chatHistoryClient.get("") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status == HttpStatusCode.OK) {
                response.body<ChatHistoryListResponse>()
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("getChatHistory", "Failed to fetch chat history", e)
            null
        }
    }

    suspend fun createChatHistory(
        title: String,
        messages: List<ChatHistoryMessagePayload>,
        token: String
    ): Result<ChatConversationPayload> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.chatHistoryClient.post("") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    ChatHistoryUpsertRequest(
                        title = title,
                        messages = messages
                    )
                )
            }

            if (response.status == HttpStatusCode.Created || response.status == HttpStatusCode.OK) {
                val payload = response.body<ChatHistoryItemResponse>()
                val data = payload.data
                if (payload.success && data != null) {
                    Result.success(data)
                } else {
                    Result.failure(IllegalStateException("Khong the tao lich su tro chuyen."))
                }
            } else {
                Result.failure(response.toApiException())
            }
        } catch (e: Exception) {
            Log.e("createChatHistory", "Failed to create chat history", e)
            Result.failure(IllegalStateException("Khong the luu lich su tro chuyen len may chu."))
        }
    }

    suspend fun updateChatHistory(
        chatId: String,
        title: String,
        messages: List<ChatHistoryMessagePayload>,
        token: String
    ): Result<ChatConversationPayload> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.chatHistoryClient.put(chatId) {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    ChatHistoryUpsertRequest(
                        title = title,
                        messages = messages
                    )
                )
            }

            if (response.status == HttpStatusCode.OK) {
                val payload = response.body<ChatHistoryItemResponse>()
                val data = payload.data
                if (payload.success && data != null) {
                    Result.success(data)
                } else {
                    Result.failure(IllegalStateException("Khong the cap nhat lich su tro chuyen."))
                }
            } else {
                Result.failure(response.toApiException())
            }
        } catch (e: Exception) {
            Log.e("updateChatHistory", "Failed to update chat history", e)
            Result.failure(IllegalStateException("Khong the dong bo lich su tro chuyen."))
        }
    }

    suspend fun deleteChatHistory(
        chatId: String,
        token: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.chatHistoryClient.delete(chatId) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            when (response.status) {
                HttpStatusCode.OK,
                HttpStatusCode.NoContent,
                HttpStatusCode.NotFound -> Result.success(Unit)

                else -> Result.failure(
                    response.toApiException()
                )
            }
        } catch (e: Exception) {
            Log.e("deleteChatHistory", "Failed to delete chat history", e)
            Result.failure(IllegalStateException("Khong the xoa lich su tro chuyen tren may chu."))
        }
    }

    suspend fun downloadHistoryImage(
        historyId: String,
        token: String
    ): DownloadedHistoryImage? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.historyClient.get("$historyId/image") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            if (response.status != HttpStatusCode.OK) {
                return@withContext null
            }

            DownloadedHistoryImage(
                bytes = response.body<ByteArray>(),
                mimeType = response.headers[HttpHeaders.ContentType]
            )
        } catch (e: Exception) {
            Log.e("downloadHistoryImage", "Failed to download history image", e)
            null
        }
    }

    suspend fun deleteUserHistory(
        historyId: String,
        token: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.historyClient.delete(historyId) {
                header(HttpHeaders.Authorization, "Bearer $token")
            }

            when (response.status) {
                HttpStatusCode.OK,
                HttpStatusCode.NoContent,
                HttpStatusCode.NotFound -> Result.success(Unit)

                else -> Result.failure(
                    response.toApiException()
                )
            }
        } catch (e: Exception) {
            Log.e("deleteUserHistory", "Failed to delete history", e)
            Result.failure(IllegalStateException("Khong the xoa lich su tren may chu."))
        }
    }

    private fun parseDetectResponse(responseBody: String): DiagnoseApiResponse? {
        return try {
            networkJson.decodeFromString<DiagnoseApiResponse>(responseBody)
        } catch (primaryError: Exception) {
            Log.w("detectImageTyped", "Primary detect parser failed, trying fallback schema", primaryError)
            try {
                val fallback = networkJson.decodeFromString<DetectFallbackResponse>(responseBody)
                fallback.toDiagnoseApiResponse()
            } catch (fallbackError: Exception) {
                Log.e("detectImageTyped", "Fallback detect parser failed", fallbackError)
                null
            }
        }
    }

    private fun ensureFileName(fileName: String, mimeType: ContentType): String {
        val hasExtension = fileName.contains('.')
        if (hasExtension) {
            return fileName
        }

        val extension = when {
            mimeType.match(ContentType.Image.JPEG) -> ".jpg"
            mimeType.match(ContentType.Image.PNG) -> ".png"
            mimeType.toString().contains("webp", ignoreCase = true) -> ".webp"
            mimeType.toString().contains("gif", ignoreCase = true) -> ".gif"
            else -> ".jpg"
        }

        return "$fileName$extension"
    }
}

data class DownloadedHistoryImage(
    val bytes: ByteArray,
    val mimeType: String? = null
)

class AuthTokenException(message: String) : IllegalStateException(message)

private suspend fun io.ktor.client.statement.HttpResponse.toApiException(): IllegalStateException {
    val message = parseApiError(bodyAsText())
    return if (status == HttpStatusCode.Unauthorized || status == HttpStatusCode.Forbidden) {
        AuthTokenException(message)
    } else {
        IllegalStateException(message)
    }
}

private fun parseApiError(responseBody: String): String {
    return runCatching {
        val payload = Json { ignoreUnknownKeys = true }
            .decodeFromString<ApiErrorResponse>(responseBody)
        payload.message ?: payload.error ?: "Yêu cầu không thành công."
    }.getOrDefault("Yêu cầu không thành công.")
}

@Serializable
private data class DetectFallbackResponse(
    val success: Boolean = false,
    val plant_name: String? = null,
    val plant_vn: String? = null,
    val detections: List<DetectFallbackItem> = emptyList(),
    val debug_info: List<String> = emptyList(),
    val cls_confidence: Double? = null
)

@Serializable
private data class DetectFallbackItem(
    val name: String,
    val confidence: Double? = null,
    val box: List<Double> = emptyList()
)

private fun DetectFallbackResponse.toDiagnoseApiResponse(): DiagnoseApiResponse? {
    if (!success || detections.isEmpty()) return null

    val normalizedNames = detections.map { detection ->
        detection.name
            .substringAfterLast('_')
            .replace('_', ' ')
            .trim()
            .ifBlank { detection.name.replace('_', ' ') }
    }

    val primaryName = normalizedNames.first()
    val debugSummary = buildList {
        plant_vn?.takeIf { it.isNotBlank() }?.let { add("Cây: $it") }
        cls_confidence?.let { add("Độ tin cậy nhận diện cây: ${(it * 100).toInt()}%") }
        addAll(debug_info)
    }.joinToString("\n")

        return DiagnoseApiResponse(
            success = true,
            data = DiagnoseData(
                diseaseName = primaryName,
                possibleProblems = normalizedNames,
                symptoms = debugSummary,
                causes = "",
                treatment = emptyList<TreatmentData>(),
                recoveryCare = emptyList<RecoveryCareData>(),
                detections = detections.map { detection ->
                    DetectionData(
                        name = detection.name,
                        confidence = detection.confidence ?: 0.0,
                        box = detection.box
                    )
                },
                resultType = "diagnosed"
            )
        )
    }
