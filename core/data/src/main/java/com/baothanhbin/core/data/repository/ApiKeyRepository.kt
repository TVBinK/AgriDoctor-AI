package com.baothanhbin.core.data.repository

import com.baothanhbin.core.datastore.ApiKeyDataStore
import com.baothanhbin.core.data.encryption.KeyEncryptionManager
import com.baothanhbin.core.network.NetworkDataSource
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository quản lý API key:
 * - Lấy từ server nếu chưa có trong cache
 * - Lưu vào DataStore đã mã hóa
 * - Trả về từ cache nếu đã có
 */
@Singleton
class ApiKeyRepository @Inject constructor(
    private val dataStore: ApiKeyDataStore,
    private val encryptionManager: KeyEncryptionManager,
    private val networkDataSource: NetworkDataSource
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
            val encryptedKey = dataStore.getApiKey()
            if (encryptedKey != null) {
                val decryptedKey = encryptionManager.decryptApiKey(encryptedKey)
                if (decryptedKey != null) {
                    Log.d(TAG, "API key retrieved from cache")
                    return decryptedKey
                } else {
                    Log.w(TAG, "Failed to decrypt cached API key, fetching from server")
                    dataStore.clearApiKey() // Clear invalid cache
                }
            }

            // Nếu không có trong cache, lấy từ server
            Log.d(TAG, "Fetching API key from server")
            val apiKey = networkDataSource.getGeminiApiKey()
            
            if (apiKey != null) {
                // Mã hóa và lưu vào DataStore
                val encrypted = encryptionManager.encryptApiKey(apiKey)
                if (encrypted != null) {
                    dataStore.saveApiKey(encrypted)
                    Log.d(TAG, "API key saved to cache")
                } else {
                    Log.e(TAG, "Failed to encrypt API key")
                }
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

