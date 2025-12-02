package com.baothanhbin.core.data.repository

import com.baothanhbin.core.datastore.SecuritySettingsDataStore
import com.baothanhbin.core.datastore.SecuritySettingsProto
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class SecuritySettingsRepository @Inject constructor(
    private val securitySettingsDataStore: SecuritySettingsDataStore
) {

    fun observeSecuritySettings(): Flow<SecuritySettingsProto> {
        return securitySettingsDataStore.securitySettingsFlow
    }

    fun observeBiometricEnabled(): Flow<Boolean> {
        return securitySettingsDataStore.securitySettingsFlow.map { it.biometricsEnabled }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        securitySettingsDataStore.setBiometricsEnabled(enabled)
    }

    suspend fun updateLastUnlockTimestamp(timestamp: Long) {
        securitySettingsDataStore.updateLastUnlockTimestamp(timestamp)
    }
}

