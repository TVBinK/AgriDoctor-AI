package com.baothanhbin.core.network

import android.util.Log
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object NetworkDataSource {

    suspend fun detectImage(
        imageBytes: ByteArray,
        fileName: String,
        mimeTypeString: String
    ): String? = detectImage(imageBytes, fileName, ContentType.parse(mimeTypeString))

    suspend fun detectImage(
        imageBytes: ByteArray,
        fileName: String = "image.jpg",
        mimeType: ContentType = ContentType.Image.JPEG
    ): String? = withContext(Dispatchers.IO) {
        try {
            Log.d("detectImage", "============================================")
            Log.d("detectImage", "REQUEST DETAILS:")
            Log.d("detectImage", "URL: http://192.168.34.116:3000/api/detect")
            Log.d("detectImage", "Method: POST")
            Log.d("detectImage", "Body Type: multipart/form-data")
            Log.d("detectImage", "============================================")
            
            Log.d("detectImage", "Original file name: $fileName")
            Log.d("detectImage", "Image size: ${imageBytes.size} bytes")
            Log.d("detectImage", "MimeType: $mimeType")
            
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
            
            Log.d("detectImage", "Final file name: $ensuredFileName")
            
            val contentTypeHeader = mimeType.toString()
            val contentDispositionHeader = "filename=\"$ensuredFileName\""
            
            Log.d("detectImage", "Form Data Key: image")
            Log.d("detectImage", "Form Data Value: [Binary data - ${imageBytes.size} bytes]")
            Log.d("detectImage", "Content-Type header: $contentTypeHeader")
            Log.d("detectImage", "Content-Disposition header: $contentDispositionHeader")
            Log.d("detectImage", "============================================")

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
            
            Log.d("detectImage", "Response status: ${response.status}")
            
            val responseBody = response.bodyAsText()
            Log.d("detectImage", "Response body: $responseBody")
            
            if (response.status == HttpStatusCode.OK) {
                Log.d("detectImage", "API call successful")
                responseBody
            } else {
                Log.e("detectImage", "API call failed with status: ${response.status}")
                null
            }
        } catch (e: Exception) {
            Log.e("detectImage", "Error calling API: ${e.message}", e)
            Log.e("detectImage", "Stack trace: ${e.stackTraceToString()}")
            null
        }
    }
}


