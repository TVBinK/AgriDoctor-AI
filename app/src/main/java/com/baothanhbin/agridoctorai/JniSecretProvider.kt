package com.baothanhbin.agridoctorai

import com.baothanhbin.core.network.SecretProvider

/**
 * JNI wrapper để lấy API key an toàn từ native code
 * API key sẽ được lưu trữ trong native C++ code để bảo mật tốt hơn
 */
object JniSecretProvider : SecretProvider {
    
    // Load native library khi class được khởi tạo
    init {
        try {
            System.loadLibrary("secrets")
        } catch (e: Exception) {
            android.util.Log.e("JniSecretProvider", "Failed to load native library", e)
        }
    }
    
    /**
     * Native method để lấy Gemini API key
     * @return Gemini API key được lưu trữ trong native code
     */
    override external fun getGeminiApiKey(): String
    
    /**
     * Native method để lấy debug model name
     * @return Debug model name
     */
    override external fun getDebugModelName(): String
    
    /**
     * Native method để lấy release model name
     * @return Release model name
     */
    override external fun getReleaseModelName(): String
}

