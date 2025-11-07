package com.baothanhbin.core.network

import android.util.Log
import com.baothanhbin.core.model.ApiKeyResponse
import com.baothanhbin.core.model.DiagnoseApiResponse
import com.baothanhbin.core.model.GeminiRequest
import com.baothanhbin.core.model.GeminiResponse
import com.baothanhbin.core.model.GeminiContent
import com.baothanhbin.core.model.GeminiPart
import io.ktor.client.call.body
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

object NetworkDataSource {
    
    suspend fun detectImageTyped(
        imageBytes: ByteArray,
        fileName: String,
        mimeTypeString: String
    ): DiagnoseApiResponse? = detectImageTyped(imageBytes, fileName, ContentType.parse(mimeTypeString))

    suspend fun detectImageTyped(
        imageBytes: ByteArray,
        fileName: String = "image.jpg",
        mimeType: ContentType = ContentType.Image.JPEG
    ): DiagnoseApiResponse? = withContext(Dispatchers.IO) {
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
            )

            if (response.status == HttpStatusCode.OK) {
                Log.d("detectImageTyped", "API call successful")
                val result: DiagnoseApiResponse = response.body()
                result
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("detectImageTyped", "Error parsing response: ${e.message}", e)
            null
        }
    }

    /**
     * Lấy Gemini API key từ server
     * Server trả về JSON: { "success": true, "data": { "apiKey": "..." } }
     */
    suspend fun getGeminiApiKey(): String? = withContext(Dispatchers.IO) {
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
}


