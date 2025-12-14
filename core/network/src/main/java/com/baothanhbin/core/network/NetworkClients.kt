package com.baothanhbin.core.network

import android.util.Log
import com.baothanhbin.core.network.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

internal object NetworkClients {

    /**
     * Base URL cho API server
     * 
     * Cấu hình sử dụng ADB reverse forwarding (kết nối qua cáp USB):
     * 1. Server chạy trên máy tính tại: https://localhost:3443
     * 2. Chạy lệnh ADB: adb reverse tcp:3443 tcp:3443
     * 3. Ứng dụng Android sẽ kết nối đến localhost:3443
     * 
     * Lợi ích:
     * - Không cần cấu hình IP, không phụ thuộc mạng WiFi/4G
     * - Kết nối ổn định qua USB
     * - Không cần lo về firewall
     */
    private val SERVER_BASE_URL: String
        get() = "https://127.0.0.1:3443"

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

    /**
     * Kiểm tra kết nối đến server
     * @return true nếu kết nối thành công, false nếu không thể kết nối
     */
    suspend fun testServerConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val testClient = createClient(
                    baseUrl = SERVER_BASE_URL,
                    acceptAllCertificates = BuildConfig.DEBUG
                )
                val response: HttpResponse = testClient.get("")
                val isSuccess = response.status.value in 200..499 // 4xx vẫn là kết nối thành công (server trả lời)
                Log.d("NetworkClients", "🔗 Test connection: ${if (isSuccess) "✅ OK" else "❌ Failed"} - Status: ${response.status.value}")
                testClient.close()
                isSuccess
            } catch (e: Exception) {
                Log.e("NetworkClients", "❌ Connection test failed: ${e.message}")
                Log.e("NetworkClients", "   Kiểm tra:")
                Log.e("NetworkClients", "   1. Đã chạy: adb reverse tcp:3443 tcp:3443")
                Log.e("NetworkClients", "   2. Server đang chạy tại: https://localhost:3443")
                Log.e("NetworkClients", "   3. Điện thoại đang kết nối qua USB")
                false
            }
        }
    }

    init {
        Log.d("NetworkClients", "🔗 ===== Network Configuration =====")
        Log.d("NetworkClients", "🔗 Server Base URL: $SERVER_BASE_URL")
        Log.d("NetworkClients", "🔗 Detect API: $SERVER_BASE_URL$API_DETECT_PATH")
        Log.d("NetworkClients", "🔗 Accept All Certificates: ${BuildConfig.DEBUG}")
        Log.d("NetworkClients", "🔗 =================================")
        Log.d("NetworkClients", "💡 Lưu ý: Sử dụng ADB reverse forwarding qua USB")
        Log.d("NetworkClients", "💡 Chạy: adb reverse tcp:3443 tcp:3443")
    }

    // Cách sử dụng ADB reverse forwarding:
    // 1. Kết nối điện thoại với máy tính qua USB
    // 2. Bật USB debugging trên điện thoại
    // 3. Chạy lệnh: adb reverse tcp:3443 tcp:3443
    // 4. Server trên máy tính chạy tại: https://localhost:3443
    // 5. Ứng dụng sẽ kết nối đến: https://127.0.0.1:3443/api/detect
    //
    // Lưu ý:
    // - Trong debug mode: acceptAllCertificates = true để chấp nhận self-signed certificates
    // - Trong release mode: acceptAllCertificates = false và sử dụng certificate hợp lệ
    // - ADB reverse chỉ hoạt động khi cáp USB đang kết nối
    // - Sử dụng testServerConnection() để kiểm tra kết nối
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



