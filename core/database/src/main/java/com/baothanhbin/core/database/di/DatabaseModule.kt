package com.baothanhbin.core.database.di

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.room.Room
import com.baothanhbin.core.database.AgriDoctorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Singleton

// SQLCipher SupportFactory
// Uncomment sau khi sync Gradle thành công:
// import net.zetetic.database.sqlcipher.SupportFactory
// Hoặc:
// import net.sqlcipher.database.SupportFactory

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val TAG = "DatabaseModule"
    private const val PASSWORD_LENGTH = 32 // 32 bytes = 256 bits
    private const val PREFS_NAME = "database_password_prefs"
    private const val KEY_DATABASE_PASSWORD = "database_password"
    private const val KEY_ENCRYPTED_DATABASE_PASSWORD = "encrypted_database_password"
    private const val KEY_MIGRATED_TO_SQLCIPHER = "migrated_to_sqlcipher"
    private const val DATABASE_NAME = "agridoctor_database"

    // Android Keystore config cho database password
    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val IV_LENGTH = 12
    private const val KEYSTORE_ALIAS_DB_PASSWORD = "db_password_encryption_key"
    
    /**
     * Lấy hoặc tạo password cho database encryption
     * Password được lưu trong SharedPreferences dưới dạng ENCRYPTED Base64 string
     * Sử dụng Android Keystore để mã hóa password (AES/GCM, hardware-backed nếu thiết bị hỗ trợ)
     */
    private fun getDatabasePassword(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Ưu tiên đọc encrypted password (mới)
        val encryptedPassword = prefs.getString(KEY_ENCRYPTED_DATABASE_PASSWORD, null)
        if (encryptedPassword != null) {
            val decryptedBase64 = decryptDatabasePasswordBase64(encryptedPassword)
            if (decryptedBase64 != null) {
                return try {
                    Base64.decode(decryptedBase64, Base64.NO_WRAP)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to decode decrypted database password, generating new one", e)
                    generateAndSavePassword(context, prefs)
                }
            } else {
                Log.e(TAG, "Failed to decrypt database password, generating new one")
                return generateAndSavePassword(context, prefs)
            }
        }

        // Backward-compatible: nếu trước đây lưu plain Base64 trong KEY_DATABASE_PASSWORD thì migrate sang dạng mã hóa
        val legacyBase64 = prefs.getString(KEY_DATABASE_PASSWORD, null)
        if (legacyBase64 != null) {
            return try {
                val passwordBytes = Base64.decode(legacyBase64, Base64.NO_WRAP)

                // Migrate: mã hóa Base64 cũ và lưu vào KEY_ENCRYPTED_DATABASE_PASSWORD
                val encrypted = encryptDatabasePasswordBase64(legacyBase64)
                if (encrypted != null) {
                    prefs.edit()
                        .putString(KEY_ENCRYPTED_DATABASE_PASSWORD, encrypted)
                        .remove(KEY_DATABASE_PASSWORD)
                        .apply()
                    Log.d(TAG, "Migrated legacy database password to encrypted storage")
                } else {
                    Log.e(TAG, "Failed to encrypt legacy database password, keeping legacy for this run")
                }
                
                passwordBytes
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode legacy database password, generating new one", e)
                generateAndSavePassword(context, prefs)
            }
        }

        // Không có password -> tạo mới
        return generateAndSavePassword(context, prefs)
    }
    
    /**
     * Tạo password mới và lưu vào SharedPreferences (đã mã hóa bằng Android Keystore)
     */
    private fun generateAndSavePassword(
        context: Context,
        prefs: SharedPreferences,
    ): ByteArray {
        val random = SecureRandom()
        val password = ByteArray(PASSWORD_LENGTH)
        random.nextBytes(password)
        
        // Encode password sang Base64 để ổn định khi convert String
        val encodedPassword = Base64.encodeToString(password, Base64.NO_WRAP)

        // Mã hóa Base64 password bằng Android Keystore
        val encrypted = encryptDatabasePasswordBase64(encodedPassword)
        if (encrypted == null) {
            // Nếu vì lý do nào đó mã hóa thất bại, vẫn lưu dạng legacy (đỡ mất DB),
            // nhưng log cảnh báo để xử lý sau.
            Log.e(TAG, "Failed to encrypt database password, falling back to legacy storage")
        prefs.edit()
            .putString(KEY_DATABASE_PASSWORD, encodedPassword)
            .apply()
        } else {
            prefs.edit()
                .putString(KEY_ENCRYPTED_DATABASE_PASSWORD, encrypted)
                .remove(KEY_DATABASE_PASSWORD)
                .apply()
        }
        
        Log.d(TAG, "Database password generated and saved (encrypted)")
        return password
    }

    /**
     * Lấy hoặc tạo SecretKey trong Android Keystore dành cho database password
     */
    private fun getOrCreateDbSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }

        val existingKey = keyStore.getEntry(KEYSTORE_ALIAS_DB_PASSWORD, null) as? KeyStore.SecretKeyEntry
        if (existingKey != null) {
            return existingKey.secretKey
        }

        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            ANDROID_KEYSTORE
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS_DB_PASSWORD,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        return keyGenerator.generateKey()
    }

    /**
     * Mã hóa Base64 password bằng Android Keystore, trả về ciphertext Base64
     */
    private fun encryptDatabasePasswordBase64(passwordBase64: String): String? {
        return try {
            val secretKey = getOrCreateDbSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(passwordBase64.toByteArray(Charsets.UTF_8))

            val combined = ByteArray(IV_LENGTH + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH)
            System.arraycopy(encryptedBytes, 0, combined, IV_LENGTH, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Error encrypting database password: ${e.message}", e)
            null
        }
    }

    /**
     * Giải mã ciphertext Base64 để lấy lại Base64 password gốc
     */
    private fun decryptDatabasePasswordBase64(encryptedText: String): String? {
        return try {
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            val iv = ByteArray(IV_LENGTH)
            val encryptedBytes = ByteArray(combined.size - IV_LENGTH)
            System.arraycopy(combined, 0, iv, 0, IV_LENGTH)
            System.arraycopy(combined, IV_LENGTH, encryptedBytes, 0, encryptedBytes.size)

            val secretKey = getOrCreateDbSecretKey()
            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error decrypting database password: ${e.message}", e)
            null
        }
    }
    
    /**
     * Xóa database cũ (unencrypted) nếu tồn tại
     * Cần thiết khi migrate từ unencrypted sang encrypted database
     */
    private fun deleteOldUnencryptedDatabase(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val migrated = prefs.getBoolean(KEY_MIGRATED_TO_SQLCIPHER, false)
        
        if (!migrated) {
            try {
                val dbFile = context.getDatabasePath(DATABASE_NAME)
                val dbWalFile = context.getDatabasePath("$DATABASE_NAME-wal")
                val dbShmFile = context.getDatabasePath("$DATABASE_NAME-shm")
                
                // Xóa database files nếu tồn tại
                if (dbFile.exists()) {
                    dbFile.delete()
                    Log.d(TAG, "Deleted old unencrypted database file")
                }
                if (dbWalFile.exists()) {
                    dbWalFile.delete()
                    Log.d(TAG, "Deleted old database WAL file")
                }
                if (dbShmFile.exists()) {
                    dbShmFile.delete()
                    Log.d(TAG, "Deleted old database SHM file")
                }
                
                // Đánh dấu đã migrate
                prefs.edit()
                    .putBoolean(KEY_MIGRATED_TO_SQLCIPHER, true)
                    .apply()
                
                Log.d(TAG, "Migration to SQLCipher completed - old database deleted")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete old database", e)
            }
        }
    }
    
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AgriDoctorDatabase {
        // Lấy password database
        val password = getDatabasePassword(context)
        
        // Tạo SupportFactory với password để mã hóa database
        // Sử dụng reflection để load SQLCipher class động
        val factory = try {
            // Thử package net.zetetic.database.sqlcipher.SupportFactory
            val supportFactoryClass = Class.forName("net.zetetic.database.sqlcipher.SupportFactory")
            val constructor = supportFactoryClass.getConstructor(ByteArray::class.java)
            constructor.newInstance(password) as? androidx.sqlite.db.SupportSQLiteOpenHelper.Factory
                ?: throw IllegalStateException("Failed to create SupportFactory instance")
        } catch (e: ClassNotFoundException) {
            // Thử package net.sqlcipher.database.SupportFactory
            try {
                val supportFactoryClass = Class.forName("net.sqlcipher.database.SupportFactory")
                val constructor = supportFactoryClass.getConstructor(ByteArray::class.java)
                constructor.newInstance(password) as? androidx.sqlite.db.SupportSQLiteOpenHelper.Factory
                    ?: throw IllegalStateException("Failed to create SupportFactory instance")
            } catch (e2: Exception) {
                Log.e(TAG, "SQLCipher not available, database will not be encrypted", e2)
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create SQLCipher SupportFactory", e)
            null
        }
        
        // Nếu SQLCipher có sẵn, xóa database cũ (unencrypted) trước khi tạo mới
        factory?.let {
            deleteOldUnencryptedDatabase(context)
        }
        
        val builder = Room.databaseBuilder(
            context,
            AgriDoctorDatabase::class.java,
            DATABASE_NAME
        )
        
        // Chỉ sử dụng SQLCipher factory nếu có
        factory?.let {
            builder.openHelperFactory(it)
        } ?: run {
            Log.w(TAG, "SQLCipher not available, using unencrypted database")
        }
        
        return builder
            .fallbackToDestructiveMigration(true)
            .build()
    }
}