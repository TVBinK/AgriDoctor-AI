package com.baothanhbin.core.database.di

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.room.Room
import com.baothanhbin.core.database.AgriDoctorDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.security.SecureRandom
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
    private const val KEY_MIGRATED_TO_SQLCIPHER = "migrated_to_sqlcipher"
    private const val DATABASE_NAME = "agridoctor_database"
    
    /**
     * Lấy hoặc tạo password cho database encryption
     * Password được lưu trong SharedPreferences (không encrypt để tránh circular dependency)
     * Trong production, nên sử dụng Android Keystore để encrypt password
     */
    private fun getDatabasePassword(context: Context): ByteArray {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPassword = prefs.getString(KEY_DATABASE_PASSWORD, null)
        
        return if (savedPassword != null) {
            // Decode base64 password
            try {
                val passwordBytes = android.util.Base64.decode(savedPassword, android.util.Base64.NO_WRAP)
                
                // DEBUG: Log password để có thể dùng trong DB Browser (CHỈ TRONG DEBUG BUILD)
                // Lưu ý: SQLCipher trong DB Browser cần password dạng string, nhưng password hiện tại là ByteArray
                // Có thể thử dùng Base64 string hoặc hex string
                try {
                    val debugClass = Class.forName("com.baothanhbin.agridoctorai.BuildConfig")
                    val debugField = debugClass.getField("DEBUG")
                    val isDebug = debugField.getBoolean(null)
                    
                    if (isDebug) {
                        Log.d(TAG, "=== DATABASE PASSWORD (DEBUG ONLY) ===")
                        Log.d(TAG, "⚠️ CHỈ SỬ DỤNG TRONG DEBUG - XÓA LOG NÀY TRONG PRODUCTION!")
                        Log.d(TAG, "")
                        
                        // Password gốc (Base64) - THỬ CÁCH NÀY TRƯỚC
                        Log.d(TAG, "1. Base64 password (THỬ TRƯỚC):")
                        Log.d(TAG, "   $savedPassword")
                        Log.d(TAG, "")
                        
                        // Hex string
                        val hexString = passwordBytes.joinToString("") { "%02x".format(it) }
                        Log.d(TAG, "2. Hex password (THỬ NẾU CÁCH 1 KHÔNG ĐƯỢC):")
                        Log.d(TAG, "   $hexString")
                        Log.d(TAG, "")
                        
                        // Thử convert sang UTF-8 string (có thể không valid)
                        try {
                            val utf8String = String(passwordBytes, Charsets.UTF_8)
                            Log.d(TAG, "3. UTF-8 string (CÓ THỂ KHÔNG VALID):")
                            Log.d(TAG, "   $utf8String")
                            Log.d(TAG, "")
                        } catch (e: Exception) {
                            Log.d(TAG, "3. UTF-8 string: Không thể convert (không phải valid UTF-8)")
                            Log.d(TAG, "")
                        }
                        
                        Log.d(TAG, "HƯỚNG DẪN:")
                        Log.d(TAG, "- Copy Base64 password (cách 1) và paste vào DB Browser")
                        Log.d(TAG, "- Nếu không được, thử Hex password (cách 2)")
                        Log.d(TAG, "- Đảm bảo chọn 'SQLCipher 4 defaults' trong DB Browser")
                        Log.d(TAG, "=== END PASSWORD ===")
                    }
                } catch (e: Exception) {
                    // Nếu không tìm thấy BuildConfig, không log (production build)
                }
                
                passwordBytes
            } catch (e: Exception) {
                Log.e(TAG, "Failed to decode database password, generating new one", e)
                generateAndSavePassword(context, prefs)
            }
        } else {
            // Tạo password mới
            generateAndSavePassword(context, prefs)
        }
    }
    
    /**
     * Tạo password mới và lưu vào SharedPreferences
     */
    private fun generateAndSavePassword(context: Context, prefs: SharedPreferences): ByteArray {
        val random = SecureRandom()
        val password = ByteArray(PASSWORD_LENGTH)
        random.nextBytes(password)
        
        // Lưu password dưới dạng base64
        val encodedPassword = android.util.Base64.encodeToString(password, android.util.Base64.NO_WRAP)
        prefs.edit()
            .putString(KEY_DATABASE_PASSWORD, encodedPassword)
            .apply()
        
        Log.d(TAG, "Database password generated and saved")
        return password
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