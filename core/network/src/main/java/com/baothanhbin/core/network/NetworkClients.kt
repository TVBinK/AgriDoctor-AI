package com.baothanhbin.core.network

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal object NetworkClients {

    private fun createClient(baseUrl: String): HttpClient {
        return HttpClient(Android) {
            defaultRequest {
                url(baseUrl)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }
                )
            }
        }
    }

    // For real device, use actual machine IP: 192.168.34.116
    // For emulator, use: http://10.0.2.2:3000/api/detect
    val detectClient: HttpClient = createClient("http://192.168.34.116:3000/api/detect")
    
    // Gemini API client
    val geminiClient: HttpClient = HttpClient(Android) {
        install(HttpTimeout) {
            requestTimeoutMillis = 60_000
        }
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                }
            )
        }
    }
}


