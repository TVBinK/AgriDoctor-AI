package com.baothanhbin.core.data.repository

import com.baothanhbin.core.datastore.ApiKeyDataStore
import com.baothanhbin.core.network.NetworkDataSource
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository quản lý API key:
 * - Lấy từ server nếu chưa có trong cache
 * - Lưu vào DataStore (không mã hóa)
 * - Trả về từ cache nếu đã có
 */
@Singleton
class ApiKeyRepository @Inject constructor(
    private val dataStore: ApiKeyDataStore,
    private val networkDataSource: NetworkDataSource,
    private val authRepository: AuthRepository
) {
    companion object {
        private const val TAG = "ApiKeyRepository"
    }

    /**
     * Lấy API key: từ cache hoặc từ server
     */
    suspend fun getApiKey(): String? {
        return try {
            // Kiểm tra cache trước
            val cachedKey = dataStore.getApiKey()
            if (cachedKey != null) {
                Log.d(TAG, "API key retrieved from cache")
                return cachedKey
            }

            // Nếu không có trong cache, lấy từ server
            Log.d(TAG, "Fetching API key from server")
            val token = authRepository.getToken()
            val apiKey = networkDataSource.getGeminiApiKey(token)
            
            if (apiKey != null) {
                // Lưu vào DataStore
                dataStore.saveApiKey(apiKey)
                Log.d(TAG, "API key saved to cache")
                apiKey
            } else {
                Log.e(TAG, "Failed to get API key from server")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting API key: ${e.message}", e)
            null
        }
    }
}

