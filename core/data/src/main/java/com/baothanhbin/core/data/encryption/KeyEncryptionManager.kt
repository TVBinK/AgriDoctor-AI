package com.baothanhbin.core.data.encryption

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Quản lý mã hóa/giải mã API key sử dụng Android Keystore
 */
class KeyEncryptionManager(private val context: Context) {

    companion object {
        private const val KEYSTORE_ALIAS_API_KEY = "api_key_encryption_key"
        private const val KEYSTORE_ALIAS_LOCATION = "location_encryption_key"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
        private const val TAG = "KeyEncryptionManager"
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    /**
     * Lấy hoặc tạo secret key từ Android Keystore
     */
    private fun getOrCreateSecretKey(alias: String): SecretKey {
        val existingKey = keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        // Tạo key mới nếu chưa có
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }
    
    /**
     * Mã hóa string generic
     */
    private fun encryptString(text: String, alias: String): String? {
        return try {
            val secretKey = getOrCreateSecretKey(alias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))

            // Kết hợp IV và encrypted data
            val combined = ByteArray(IV_LENGTH + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH)
            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting string with alias $alias: ${e.message}", e)
            null
        }
    }
    
    /**
     * Giải mã string generic
     */
    private fun decryptString(encryptedText: String, alias: String): String? {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // Tách IV và encrypted data
            val iv = ByteArray(IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)
            System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)

            val secretKey = getOrCreateSecretKey(alias)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting string with alias $alias: ${e.message}", e)
            null
        }
    }

    /**
     * Mã hóa API key
     */
    fun encryptApiKey(apiKey: String): String? {
        return encryptString(apiKey, KEYSTORE_ALIAS_API_KEY)
    }

    /**
     * Giải mã API key
     */
    fun decryptApiKey(encryptedApiKey: String): String? {
        return decryptString(encryptedApiKey, KEYSTORE_ALIAS_API_KEY)
    }
    
    /**
     * Mã hóa location (địa chỉ)
     */
    fun encryptLocation(location: String): String? {
        return encryptString(location, KEYSTORE_ALIAS_LOCATION)
    }
    
    /**
     * Giải mã location (địa chỉ)
     */
    fun decryptLocation(encryptedLocation: String): String? {
        return decryptString(encryptedLocation, KEYSTORE_ALIAS_LOCATION)
    }
}

