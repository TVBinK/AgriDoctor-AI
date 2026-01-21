package com.baothanhbin.core.network

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkClients {
    private fun createTrustAllManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
        }
    }

    private fun createClient(baseUrl: String): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                preconfigured = OkHttpClient.Builder().apply {
                    if (BuildConfig.DEBUG) {
                        val trustAllCerts = arrayOf<TrustManager>(createTrustAllManager())
                        val sslContext = SSLContext.getInstance("TLS")
                        sslContext.init(null, trustAllCerts, SecureRandom())
                        sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                        hostnameVerifier { _, _ -> true }
                        Log.w("NetworkClients", "⚠️ Accepting all certificates - DEBUG MODE")
                    }
                }.build()
            }
            defaultRequest {
                url(baseUrl)
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 60_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }

    val detectClient = createClient("https://localhost:3443/api/detect")
    val apiKeyClient = createClient("https://localhost:3443/api/gemini-key")
    val diseasesClient = createClient("https://localhost:3443/api/diseases")
    val classifyClient = createClient("https://localhost:3443/api/classify")
    val authClient = createClient("https://localhost:3443/api/auth/")
}



