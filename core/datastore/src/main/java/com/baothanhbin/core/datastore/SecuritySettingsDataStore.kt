package com.baothanhbin.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

object SecuritySettingsSerializer : Serializer<SecuritySettingsProto> {
    override val defaultValue: SecuritySettingsProto = SecuritySettingsProto.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SecuritySettingsProto {
        return SecuritySettingsProto.parseFrom(input)
    }

    override suspend fun writeTo(t: SecuritySettingsProto, output: OutputStream) {
        t.writeTo(output)
    }
}

class SecuritySettingsDataStore @Inject constructor(
    private val context: Context
) {

    private val Context.securitySettingsStore: DataStore<SecuritySettingsProto> by dataStore(
        fileName = "security_settings.pb",
        serializer = SecuritySettingsSerializer
    )

    val securitySettingsFlow: Flow<SecuritySettingsProto> = context.securitySettingsStore.data
        .catch { emit(SecuritySettingsProto.getDefaultInstance()) }

    suspend fun setBiometricsEnabled(enabled: Boolean) {
        context.securitySettingsStore.updateData { proto ->
            proto.toBuilder()
                .setBiometricsEnabled(enabled)
                .build()
        }
    }

    suspend fun updateLastUnlockTimestamp(timestamp: Long) {
        context.securitySettingsStore.updateData { proto ->
            proto.toBuilder()
                .setLastUnlockTimestamp(timestamp)
                .build()
        }
    }
}

