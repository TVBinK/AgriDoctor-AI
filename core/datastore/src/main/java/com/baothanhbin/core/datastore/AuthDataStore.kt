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

    suspend fun saveToken(token: String) {
        val didPersist = withContext(Dispatchers.IO) {
            preferences.edit()
                .putString(KEY_JWT_TOKEN, token)
                .putLong(KEY_TIMESTAMP, System.currentTimeMillis())
                .commit()
        }

        if (!didPersist) {
            throw IllegalStateException("Khong the luu phien dang nhap.")
        }

        isLoggedInState.value = token.isNotEmpty()
    }

    suspend fun getToken(): String? {
        return readToken()
    }

    fun isLoggedInFlow(): Flow<Boolean> = isLoggedInState.asStateFlow()

    suspend fun clearToken() {
        withContext(Dispatchers.IO) {
            preferences.edit()
                .remove(KEY_JWT_TOKEN)
                .remove(KEY_TIMESTAMP)
                .commit()
        }
        isLoggedInState.value = false
    }

    private fun readToken(): String? {
        return preferences.getString(KEY_JWT_TOKEN, null)
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
        const val KEY_TIMESTAMP = "timestamp"
    }
}
