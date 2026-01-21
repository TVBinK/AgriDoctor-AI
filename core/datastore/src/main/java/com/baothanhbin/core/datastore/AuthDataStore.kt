package com.baothanhbin.core.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

/**
 * Serializer cho AuthProto
 */
object AuthSerializer : Serializer<AuthProto> {
    override val defaultValue: AuthProto = AuthProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AuthProto {
        return AuthProto.parseFrom(input)
    }

    override suspend fun writeTo(t: AuthProto, output: OutputStream) {
        t.writeTo(output)
    }
}

/**
 * DataStore cho Auth token
 */
class AuthDataStore(private val context: Context) {
    private val Context.authDataStore: DataStore<AuthProto> by dataStore(
        fileName = "auth.pb",
        serializer = AuthSerializer
    )

    suspend fun saveToken(token: String) {
        context.authDataStore.updateData { current ->
            current.toBuilder()
                .setJwtToken(token)
                .setTimestamp(System.currentTimeMillis())
                .build()
        }
    }

    suspend fun getToken(): String? {
        return try {
            context.authDataStore.data
                .catch {
                    Log.e("AuthDataStore", "Error getting token", it)
                    emit(AuthProto.getDefaultInstance())
                }
                .map { proto ->
                    proto.jwtToken.ifEmpty { null }
                }
                .first()
        } catch (e: Exception) {
            Log.e("AuthDataStore", "Error reading token", e)
            null
        }
    }

    fun isLoggedInFlow(): Flow<Boolean> {
        return context.authDataStore.data
            .catch {
                Log.e("AuthDataStore", "Error checking login status", it)
                emit(AuthProto.getDefaultInstance())
            }
            .map { proto ->
                proto.jwtToken.isNotEmpty()
            }
    }

    suspend fun clearToken() {
        context.authDataStore.updateData {
            AuthProto.getDefaultInstance()
        }
    }
}
