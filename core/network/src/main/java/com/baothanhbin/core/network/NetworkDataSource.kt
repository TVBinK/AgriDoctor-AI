package com.baothanhbin.core.network

import android.util.Log
import com.baothanhbin.core.model.ApiErrorResponse
import com.baothanhbin.core.model.ChatbotHistoryMessage
import com.baothanhbin.core.model.ChatbotRequest
import com.baothanhbin.core.model.ChatbotResponse
import com.baothanhbin.core.model.ClassifyApiResponse
import com.baothanhbin.core.model.DetectionData
import com.baothanhbin.core.model.DiagnoseApiResponse
import com.baothanhbin.core.model.DiagnoseData
import com.baothanhbin.core.model.RecoveryCareData
import com.baothanhbin.core.model.TreatmentData
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.post
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
        token: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.chatbotClient.post("") {
                header(HttpHeaders.Authorization, "Bearer $token")
                contentType(ContentType.Application.Json)
                setBody(
                    ChatbotRequest(
                        message = message,
                        history = history
                    )
                )
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
                Result.failure(IllegalStateException(parseApiError(response.bodyAsText())))
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
            }
        )
    )
}
