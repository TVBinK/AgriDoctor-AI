package com.baothanhbin.core.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer cho ApiKeyProto
 */
object ApiKeySerializer : Serializer<ApiKeyProto> {
    override val defaultValue: ApiKeyProto = ApiKeyProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ApiKeyProto {
        return ApiKeyProto.parseFrom(input)
    }

    override suspend fun writeTo(t: ApiKeyProto, output: OutputStream) {
        t.writeTo(output)
    }
}

/**
 * DataStore cho API key
 */
class ApiKeyDataStore(private val context: Context) {
    private val Context.apiKeyDataStore: DataStore<ApiKeyProto> by dataStore(
        fileName = "api_key.pb",
        serializer = ApiKeySerializer
    )

    suspend fun saveApiKey(encryptedKey: String) {
        context.apiKeyDataStore.updateData { current ->
            current.toBuilder()
                .setEncryptedKey(encryptedKey)
                .setTimestamp(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun getApiKey(): String? {
        return try {
            context.apiKeyDataStore.data
                .catch {
                    Log.e("ApiKeyDataStore", "Error getting API key", it)
                    emit(ApiKeyProto.getDefaultInstance())
                }
                .map { proto ->
                    proto.encryptedKey.ifEmpty { null }
                }
                .first()
        } catch (e: Exception) {
            Log.e("ApiKeyDataStore", "Error reading API key", e)
            null
        }
    }

    suspend fun clearApiKey() {
        context.apiKeyDataStore.updateData {
            ApiKeyProto.getDefaultInstance()
        }
    }
}

