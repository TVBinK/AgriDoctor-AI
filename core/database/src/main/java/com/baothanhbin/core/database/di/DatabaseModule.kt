package com.baothanhbin.core.database.di

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteOpenHelper
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

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    private const val TAG = "DatabaseModule"
    private const val DATABASE_NAME = "agridoctor_database"
    private const val PREFS_NAME = "db_password"
    private const val KEY_PASSWORD = "encrypted_password"
    private const val KEYSTORE_ALIAS = "db_key"
    private const val KEYSTORE_TYPE = "AndroidKeyStore"
    private const val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding"
    private const val PASSWORD_SIZE = 32 // 32 bytes = 256 bits
    
    /**
     * Lấy hoặc tạo password cho database
     */
    private fun getDatabasePassword(context: Context): ByteArray {
        //Lấy password đã lưu trong SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val encrypted = prefs.getString(KEY_PASSWORD, null)
        //Nếu có password đã lưu, giải mã và trả về
        if (encrypted != null) {
            val decrypted = decryptPassword(encrypted)
            if (decrypted != null) {
                return Base64.decode(decrypted, Base64.NO_WRAP)
            }
        }
        
        // Tạo password mới
        val password = ByteArray(PASSWORD_SIZE).apply {
            SecureRandom().nextBytes(this)
        }
        
        val encryptedPassword = encryptPassword(Base64.encodeToString(password, Base64.NO_WRAP))
        prefs.edit().putString(KEY_PASSWORD, encryptedPassword).apply()

        return password
    }
    
    /**
     * Mã hóa password bằng Android Keystore
     */
    private fun encryptPassword(password: String): String? {
        return try {
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            
            val iv = cipher.iv
            val encrypted = cipher.doFinal(password.toByteArray())
            val combined = iv + encrypted
            
            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Encrypt password failed", e)
            null
        }
    }
    
    /**
     * Giải mã password từ Android Keystore
     */
    private fun decryptPassword(encrypted: String): String? {
        return try {
            val combined = Base64.decode(encrypted, Base64.NO_WRAP)
            val iv = combined.sliceArray(0..11)
            val data = combined.sliceArray(12 until combined.size)
            
            val key = getOrCreateKey()
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, iv))
            
            String(cipher.doFinal(data))
        } catch (e: Exception) {
            Log.e(TAG, "Decrypt password failed", e)
            null
        }
    }
    
    /**
     * Lấy hoặc tạo key mà thực chất được quản lý bởi Android Keystore
     */
    private fun getOrCreateKey(): SecretKey {
        // mở keystore
        val keyStore = KeyStore.getInstance(KEYSTORE_TYPE).apply { load(null) }
        // kiểm tra key đã tồn tại chưa
        val existing = keyStore.getEntry(KEYSTORE_ALIAS, null) as? KeyStore.SecretKeyEntry
        if (existing != null) return existing.secretKey // trả về key nếu đã tồn tại
        // tạo key mới nếu chưa tồn tại
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_TYPE)
        val spec = KeyGenParameterSpec.Builder(
            KEYSTORE_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()
        
        generator.init(spec)
        return generator.generateKey()
    }
    
    // tạo ra 1 SQLCipher factory có thể truyền pwd để room de,en .db
    private fun createSQLCipherFactory(password: ByteArray): SupportSQLiteOpenHelper.Factory? {
        val candidates = listOf(
            "net.zetetic.database.sqlcipher.SupportFactory",
            "net.sqlcipher.database.SupportFactory"
        )
        // thử lần lượt các class name để tìm factory phù hợp
        for (className in candidates) {
            try {
                val factoryClass = Class.forName(className)
                // nếu tìm thấy thì tạo instance với pwd
                val constructor = factoryClass.getConstructor(ByteArray::class.java)
                return constructor.newInstance(password) as? SupportSQLiteOpenHelper.Factory
            } catch (_: ClassNotFoundException) {

            } catch (e: Exception) {

            }
        }
        return null
    }
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AgriDoctorDatabase {
        val password = getDatabasePassword(context)
        val factory = createSQLCipherFactory(password)
        
        return Room.databaseBuilder(context, AgriDoctorDatabase::class.java, DATABASE_NAME)
            //Room sẽ dùng factory do bạn cung cấp để tạo SupportSQLiteOpenHelper khi gọi hàm openHelperFactory
            .apply {
                factory?.let { openHelperFactory(it) }
            }
            .fallbackToDestructiveMigration()
            .build()
    }
}