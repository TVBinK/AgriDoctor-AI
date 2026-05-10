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
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object NetworkClients {
    // EC2 is currently exposed through Nginx on port 80.
    // Replace with your HTTPS domain later when SSL is configured.
    private const val API_BASE_URL = "http://54.173.14.193"

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
                    connectTimeout(30, TimeUnit.SECONDS)
                    readTimeout(120, TimeUnit.SECONDS)
                    writeTimeout(120, TimeUnit.SECONDS)
                    callTimeout(120, TimeUnit.SECONDS)
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
                requestTimeoutMillis = 120_000
                connectTimeoutMillis = 30_000
                socketTimeoutMillis = 120_000
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }
        }
    }

    val detectClient = createClient("$API_BASE_URL/api/detect")
    val apiKeyClient = createClient("$API_BASE_URL/api/gemini-key")
    val diseasesClient = createClient("$API_BASE_URL/api/diseases")
    val classifyClient = createClient("$API_BASE_URL/api/classify")
    val authClient = createClient("$API_BASE_URL/api/auth/")
}



