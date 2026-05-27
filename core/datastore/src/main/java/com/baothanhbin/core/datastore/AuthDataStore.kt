package com.baothanhbin.core.datastore

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataMigration
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthDataStore(context: Context) {
    private val appContext = context.applicationContext
    private val authDataStore: DataStore<AuthProto> = appContext.authDataStore

    suspend fun saveToken(token: String) {
        authDataStore.updateData { current ->
            current.toBuilder()
                .setJwtToken(token.trim())
                .clearUserId()
                .setTimestamp(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun saveSession(token: String, userId: String?) {
        val normalizedUserId = userId?.trim().orEmpty()
        authDataStore.updateData { current ->
            current.toBuilder()
                .setJwtToken(token.trim())
                .setUserId(normalizedUserId)
                .setTimestamp(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun saveUserId(userId: String) {
        val normalizedUserId = userId.trim()
        authDataStore.updateData { current ->
            current.toBuilder()
                .setUserId(normalizedUserId)
                .build()
        }
    }

    suspend fun getToken(): String? {
        return authDataStore.data
            .first()
            .jwtToken
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    suspend fun getUserId(): String? {
        return authDataStore.data
            .first()
            .userId
            .trim()
            .takeIf { it.isNotEmpty() }
    }

    fun isLoggedInFlow(): Flow<Boolean> = dataFlow()
        .map { auth -> auth.jwtToken.isNotBlank() }

    fun currentUserIdFlow(): Flow<String?> = dataFlow()
        .map { auth -> auth.userId.trim().takeIf { it.isNotEmpty() } }

    suspend fun clearToken() {
        authDataStore.updateData {
            AuthProto.getDefaultInstance()
        }
    }

    private fun dataFlow(): Flow<AuthProto> {
        return authDataStore.data.catch { error ->
            if (error is IOException) {
                emit(AuthProto.getDefaultInstance())
            } else {
                throw error
            }
        }
    }
}

private object AuthProtoSerializer : Serializer<AuthProto> {
    override val defaultValue: AuthProto = AuthProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AuthProto {
        return try {
            AuthProto.parseFrom(input)
        } catch (error: IOException) {
            throw androidx.datastore.core.CorruptionException("Cannot read auth proto.", error)
        }
    }

    override suspend fun writeTo(t: AuthProto, output: OutputStream) {
        t.writeTo(output)
    }
}

private class AuthEncryptedPrefsMigration(
    private val context: Context,
) : DataMigration<AuthProto> {

    override suspend fun shouldMigrate(currentData: AuthProto): Boolean {
        if (!currentData.isDefaultState()) {
            return false
        }
        return readLegacyAuth().hasAnyPersistedValue()
    }

    override suspend fun migrate(currentData: AuthProto): AuthProto {
        val legacyAuth = readLegacyAuth()
        if (!legacyAuth.hasAnyPersistedValue()) {
            return currentData
        }

        return currentData.toBuilder()
            .setJwtToken(legacyAuth.jwtToken)
            .setTimestamp(legacyAuth.timestamp)
            .setUserId(legacyAuth.userId)
            .build()
    }

    override suspend fun cleanUp() {
        runCatching {
            context.deleteSharedPreferences(LEGACY_PREFS_FILE_NAME)
        }
    }

    private fun readLegacyAuth(): LegacyAuthSnapshot {
        val preferences = runCatching {
            createLegacyEncryptedPreferences(context)
        }.getOrNull() ?: return LegacyAuthSnapshot()

        return LegacyAuthSnapshot(
            jwtToken = preferences.getString(KEY_JWT_TOKEN, null).orEmpty().trim(),
            userId = preferences.getString(KEY_USER_ID, null).orEmpty().trim(),
            timestamp = preferences.getLong(KEY_TIMESTAMP, 0L),
        )
    }
}

private data class LegacyAuthSnapshot(
    val jwtToken: String = "",
    val userId: String = "",
    val timestamp: Long = 0L,
) {
    fun hasAnyPersistedValue(): Boolean {
        return jwtToken.isNotEmpty() || userId.isNotEmpty() || timestamp > 0L
    }
}

private fun AuthProto.isDefaultState(): Boolean {
    return jwtToken.isBlank() && userId.isBlank() && timestamp == 0L
}

private fun createLegacyEncryptedPreferences(context: Context): SharedPreferences {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    return EncryptedSharedPreferences.create(
        context,
        LEGACY_PREFS_FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )
}

private val Context.authDataStore: DataStore<AuthProto> by dataStore(
    fileName = "auth.pb",
    serializer = AuthProtoSerializer,
    produceMigrations = { context ->
        listOf(AuthEncryptedPrefsMigration(context))
    },
)

private const val LEGACY_PREFS_FILE_NAME = "secure_auth_prefs"
private const val KEY_JWT_TOKEN = "jwt_token"
private const val KEY_USER_ID = "user_id"
private const val KEY_TIMESTAMP = "timestamp"
