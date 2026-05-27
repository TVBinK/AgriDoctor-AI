package com.baothanhbin.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class AuthDataStore(context: Context) {
    private val appContext = context.applicationContext
    private val preferences: SharedPreferences = createEncryptedPreferences(appContext)
    private val isLoggedInState = MutableStateFlow(!readToken().isNullOrEmpty())
    private val currentUserIdState = MutableStateFlow(readUserId())

    suspend fun saveToken(token: String) {
        val didPersist = withContext(Dispatchers.IO) {
            preferences.edit()
                .putString(KEY_JWT_TOKEN, token)
                .remove(KEY_USER_ID)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .commit()
        }

        if (!didPersist) {
            throw IllegalStateException("Khong the luu phien dang nhap.")
        }

        isLoggedInState.value = token.isNotEmpty()
        currentUserIdState.value = null
    }

    suspend fun saveSession(token: String, userId: String?) {
        val normalizedUserId = userId?.trim().orEmpty()
        val didPersist = withContext(Dispatchers.IO) {
            preferences.edit()
                .putString(KEY_JWT_TOKEN, token)
                .putString(KEY_USER_ID, normalizedUserId.ifEmpty { null })
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .commit()
        }

        if (!didPersist) {
            throw IllegalStateException("Khong the luu phien dang nhap.")
        }

        isLoggedInState.value = token.isNotEmpty()
        currentUserIdState.value = normalizedUserId.ifEmpty { null }
    }

    suspend fun saveUserId(userId: String) {
        val normalizedUserId = userId.trim()
        val didPersist = withContext(Dispatchers.IO) {
            preferences.edit()
                .putString(KEY_USER_ID, normalizedUserId.ifEmpty { null })
                .commit()
        }

        if (!didPersist) {
            throw IllegalStateException("Khong the luu user id hien tai.")
        }

        currentUserIdState.value = normalizedUserId.ifEmpty { null }
    }

    suspend fun getToken(): String? {
        return readToken()
    }

    suspend fun getUserId(): String? {
        return readUserId()
    }

    fun isLoggedInFlow(): Flow<Boolean> = isLoggedInState.asStateFlow()

    fun currentUserIdFlow(): Flow<String?> = currentUserIdState.asStateFlow()

    suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            preferences.edit()
                .remove(KEY_JWT_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_TIMESTAMP)
                .commit()
        }
        isLoggedInState.value = false
        currentUserIdState.value = null
    }

    private fun readToken(): String? {
        return preferences.getString(KEY_JWT_TOKEN, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun readUserId(): String? {
        return preferences.getString(KEY_USER_ID, null)
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun createEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private companion object {
        const val PREFS_FILE_NAME = "secure_auth_prefs"
        const val KEY_JWT_TOKEN = "jwt_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_TIMESTAMP = "timestamp"
    }
}
