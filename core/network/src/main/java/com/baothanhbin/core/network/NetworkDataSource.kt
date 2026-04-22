package com.baothanhbin.core.network

import android.util.Log
import com.baothanhbin.core.model.ApiKeyResponse
import com.baothanhbin.core.model.DiagnoseApiResponse
import com.baothanhbin.core.model.DiagnoseData
import com.baothanhbin.core.model.ClassifyApiResponse
import com.baothanhbin.core.model.DetectionData
import com.baothanhbin.core.model.GeminiRequest
import com.baothanhbin.core.model.GeminiResponse
import com.baothanhbin.core.model.GeminiContent
import com.baothanhbin.core.model.GeminiPart
import com.baothanhbin.core.model.RecoveryCareData
import com.baothanhbin.core.model.TreatmentData
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.SocketTimeoutException

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
            val startedAt = System.currentTimeMillis()
            Log.d("detectImageTyped", "Starting API call")
            val ensuredFileName = run {
                val hasExt = fileName.contains('.')
                if (hasExt) fileName else {
                    val mimeString = mimeType.toString().lowercase()
                    val extension = when {
                        mimeString.contains("jpeg") || mimeString.contains("jpg") -> ".jpg"
                        mimeString.contains("png") -> ".png"
                        mimeString.contains("webp") -> ".webp"
                        mimeString.contains("gif") -> ".gif"
                        else -> ".jpg"
                    }
                    "$fileName$extension"
                }
            }
            Log.d("detectImageTyped", "Ensured file name: $ensuredFileName")
            val contentTypeHeader = mimeType.toString()
            val contentDispositionHeader = "filename=\"$ensuredFileName\""
            val response = NetworkClients.detectClient.submitFormWithBinaryData(
                formData = formData {
                    append(
                        key = "image",
                        value = imageBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, contentTypeHeader)
                            append(HttpHeaders.ContentDisposition, contentDispositionHeader)
                        }
                    )
                }
            ) {
                if (token != null) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }
            Log.d("detectImageTyped", "${response}")
            Log.d(
                "detectImageTyped",
                "API call completed with status: ${response.status} in ${System.currentTimeMillis() - startedAt} ms"
            )
            if (response.status == HttpStatusCode.OK) {
                Log.d("detectImageTyped", "API call successful")
                val responseBody = response.bodyAsText()
                Log.d("detectImageTyped", "Raw detect response: $responseBody")
                parseDetectResponse(responseBody)
            } else {
                null
            }
        } catch (e: HttpRequestTimeoutException) {
            Log.e("detectImageTyped", "Request timeout after 60000 ms: ${e.message}", e)
            null
        } catch (e: SocketTimeoutException) {
            Log.e("detectImageTyped", "Socket timeout: ${e.message}", e)
            null
        } catch (e: Exception) {
            Log.e("detectImageTyped", "Error parsing response: ${e.message}", e)
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
                Log.e("detectImageTyped", "Fallback detect parser failed: ${fallbackError.message}", fallbackError)
                null
            }
        }
    }

    /**
     * Lấy Gemini API key từ server
     * Server trả về JSON: { "success": true, "data": { "apiKey": "..." } }
     */
    suspend fun getGeminiApiKey(token: String? = null): String? = withContext(Dispatchers.IO) {
        try {
            val response = NetworkClients.apiKeyClient.get("")
            
            if (response.status == HttpStatusCode.OK) {
                val apiKeyResponse: ApiKeyResponse = response.body()
                
                if (apiKeyResponse.success && apiKeyResponse.data.apiKey.isNotEmpty()) {
                    return@withContext apiKeyResponse.data.apiKey
                } else {
                    return@withContext null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("getGeminiApiKey", "Error getting API key: ${e.message}", e)
            null
        }
    }

    /**
     * Lấy danh sách bệnh cây (raw JSON) từ API /api/diseases.
     * Trả về String JSON để lưu vào Room, tránh phụ thuộc chặt vào schema server.
     */
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
            Log.e("getDiseasesJson", "Error getting diseases: ${e.message}", e)
            null
        }
    }
    
    /**
     * Nhận diện cây từ ảnh
     * Gọi API /api/classify với ảnh được upload
     */
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
            val ensuredFileName = run {
                val hasExt = fileName.contains('.')
                if (hasExt) fileName else {
                    val mimeString = mimeType.toString().lowercase()
                    val extension = when {
                        mimeString.contains("jpeg") || mimeString.contains("jpg") -> ".jpg"
                        mimeString.contains("png") -> ".png"
                        mimeString.contains("webp") -> ".webp"
                        mimeString.contains("gif") -> ".gif"
                        else -> ".jpg"
                    }
                    "$fileName$extension"
                }
            }
            val contentTypeHeader = mimeType.toString()
            val contentDispositionHeader = "filename=\"$ensuredFileName\""
            val response = NetworkClients.classifyClient.submitFormWithBinaryData(
                formData = formData {
                    append(
                        key = "image",
                        value = imageBytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentType, contentTypeHeader)
                            append(HttpHeaders.ContentDisposition, contentDispositionHeader)
                        }
                    )
                }
            ) {
                if (token != null) {
                    headers.append(HttpHeaders.Authorization, "Bearer $token")
                }
            }

            if (response.status == HttpStatusCode.OK) {
                Log.d("classifyImageTyped", "API call successful")
                val result: ClassifyApiResponse = response.body()
                result
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("classifyImageTyped", "Error parsing response: ${e.message}", e)
            null
        }
    }
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


