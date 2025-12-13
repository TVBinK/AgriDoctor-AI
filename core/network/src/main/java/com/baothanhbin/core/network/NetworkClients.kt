package com.baothanhbin.core.network

import android.util.Log
import com.baothanhbin.core.network.BuildConfig
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

internal object NetworkClients {

    // API Endpoints - Obfuscated để tránh dịch ngược
    // Chia nhỏ string để khó đọc hơn khi decompile
    private val SERVER_BASE_URL: String
        get() = buildString {
            append("https://")
            append("192.168.34")
            append(".116:")
            append("3443")
        }

    /**
     * Tạo TrustManager chấp nhận tất cả certificates (chỉ dùng cho development)
     * CẢNH BÁO: Không sử dụng trong production!
     */
    private fun createTrustAllManager(): X509TrustManager {
        return object : X509TrustManager {
            override fun checkClientTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
                // Trust all client certificates
            }

            override fun checkServerTrusted(
                chain: Array<out X509Certificate>?,
                authType: String?
            ) {
                // Trust all server certificates (chỉ cho development)
            }

            override fun getAcceptedIssuers(): Array<X509Certificate> {
                return arrayOf()
            }
        }
    }

    private fun createClient(
        baseUrl: String,
        acceptAllCertificates: Boolean = false
    ): HttpClient {
        return HttpClient(OkHttp) {
            engine {
                // Cấu hình OkHttp client
                preconfigured = OkHttpClient.Builder().apply {
                    if (acceptAllCertificates) {
                        // Chỉ sử dụng trong debug mode - chấp nhận tất cả certificates
                        // CẢNH BÁO: Không sử dụng trong production!
                        val trustAllCerts = arrayOf<TrustManager>(createTrustAllManager())
                        val sslContext = SSLContext.getInstance("TLS")
                        sslContext.init(null, trustAllCerts, SecureRandom())
                        
                        sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
                        hostnameVerifier { _, _ -> true }
                        
                        Log.w("NetworkClients", "⚠️ Accepting all certificates - DEVELOPMENT MODE ONLY")
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
                json(
                    Json {
                        ignoreUnknownKeys = true
                        encodeDefaults = true
                    }
                )
            }
        }
    }

    // API paths - Obfuscated để tránh dịch ngược
    private val API_DETECT_PATH: String
        get() = buildString {
            append("/api")
            append("/detect")
        }
    
    private val API_KEY_PATH: String
        get() = buildString {
            append("/api")
            append("/gemini")
            append("-key")
        }

    private val API_DISEASES_PATH: String
        get() = buildString {
            append("/api")
            append("/diseases")
        }
    
    private val API_CLASSIFY_PATH: String
        get() = buildString {
            append("/api")
            append("/classify")
        }

    // For real device, use actual machine IP: 192.168.34.116
    // For emulator, use: https://10.0.2.2:3000/api/detect
    //
    // Lưu ý:
    // - Trong debug mode: acceptAllCertificates = true để chấp nhận self-signed certificates
    // - Trong release mode: acceptAllCertificates = false và sử dụng certificate hợp lệ
    // - Hoặc cài đặt self-signed certificate vào thiết bị và sử dụng network security config
    val detectClient: HttpClient = createClient(
        baseUrl = "$SERVER_BASE_URL$API_DETECT_PATH",
        acceptAllCertificates = BuildConfig.DEBUG // Chỉ chấp nhận self-signed trong debug mode
    )

    // Client cho API key endpoint
    val apiKeyClient: HttpClient = createClient(
        baseUrl = "$SERVER_BASE_URL$API_KEY_PATH",
        acceptAllCertificates = BuildConfig.DEBUG
    )

    // Client cho API danh sách bệnh
    val diseasesClient: HttpClient = createClient(
        baseUrl = "$SERVER_BASE_URL$API_DISEASES_PATH",
        acceptAllCertificates = BuildConfig.DEBUG
    )
    
    // Client cho API nhận diện cây
    val classifyClient: HttpClient = createClient(
        baseUrl = "$SERVER_BASE_URL$API_CLASSIFY_PATH",
        acceptAllCertificates = BuildConfig.DEBUG
    )
}


