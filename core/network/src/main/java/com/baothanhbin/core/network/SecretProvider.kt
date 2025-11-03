package com.baothanhbin.core.network

/**
 * Interface để cung cấp secrets một cách an toàn
 * Có thể được implement bằng nhiều cách khác nhau (JNI, obfuscation, etc.)
 */
interface SecretProvider {
    /**
     * Lấy Gemini API key
     * @return Gemini API key
     */
    fun getGeminiApiKey(): String
    
    /**
     * Lấy debug model name
     * @return Debug model name
     */
    fun getDebugModelName(): String
    
    /**
     * Lấy release model name
     * @return Release model name
     */
    fun getReleaseModelName(): String
}

