# 🔒 Tài Liệu Bảo Mật - AgriDoctorAI

Tài liệu này mô tả chi tiết các kỹ thuật bảo mật được triển khai trong ứng dụng AgriDoctorAI, bao gồm mã hóa dữ liệu, bảo vệ mã nguồn và chống dịch ngược.

---

## 📋 Mục Lục

1. [Mã Hóa Dữ Liệu Lưu Trữ](#1-mã-hóa-dữ-liệu-lưu-trữ)
2. [Mã Hóa Dữ Liệu Trong Phiên Làm Việc](#2-mã-hóa-dữ-liệu-trong-phiên-làm-việc)
3. [Bảo Vệ Mã Nguồn](#3-bảo-vệ-mã-nguồn)
4. [Chống Dịch Ngược Bằng Cách Ký Khi Build](#4-chống-dịch-ngược-bằng-cách-ký-khi-build)

---

## 1. Mã Hóa Dữ Liệu Lưu Trữ

### 1.1. Tổng Quan

Ứng dụng sử dụng **SQLCipher** để mã hóa toàn bộ database ở mức file, kết hợp với **Android Keystore** để bảo vệ password database.

### 1.2. Kiến Trúc Mã Hóa

```
┌─────────────────────────────────────────────────────────┐
│  Database (SQLCipher - AES-256)                        │
│  ┌───────────────────────────────────────────────────┐ │
│  │ Password (32 bytes, random)                       │ │
│  │ ↓                                                  │ │
│  │ Encrypted bằng Android Keystore (AES/GCM)        │ │
│  │ ↓                                                  │ │
│  │ Lưu trong SharedPreferences                       │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 1.3. Cách Triển Khai

#### 1.3.1. SQLCipher Database

**File:** `core/database/src/main/java/com/baothanhbin/core/database/di/DatabaseModule.kt`

```kotlin
@Provides
@Singleton
fun provideDatabase(@ApplicationContext context: Context): AgriDoctorDatabase {
    val password = getDatabasePassword(context)  // Lấy password đã mã hóa
    val factory = createSQLCipherFactory(password)
    
    return Room.databaseBuilder(context, AgriDoctorDatabase::class.java, DATABASE_NAME)
        .apply {
            factory?.let { openHelperFactory(it) }  // Sử dụng SQLCipher factory
        }
        .fallbackToDestructiveMigration()
        .build()
}
```

**Đặc điểm:**
- Database được mã hóa AES-256 ở mức file
- Không thể đọc database bằng SQLite thông thường
- Password được tạo ngẫu nhiên 32 bytes bằng `SecureRandom`

#### 1.3.2. Android Keystore cho Database Password

**File:** `core/database/src/main/java/com/baothanhbin/core/database/di/DatabaseModule.kt`

```kotlin
private fun getDatabasePassword(context: Context): ByteArray {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val encrypted = prefs.getString(KEY_PASSWORD, null)
    
    if (encrypted != null) {
        val decrypted = decryptPassword(encrypted)
        if (decrypted != null) {
            return Base64.decode(decrypted, Base64.NO_WRAP)
        }
    }
    
    // Tạo password mới nếu chưa có
    val password = ByteArray(PASSWORD_SIZE).apply {
        SecureRandom().nextBytes(this)
    }
    
    val encryptedPassword = encryptPassword(Base64.encodeToString(password, Base64.NO_WRAP))
    prefs.edit().putString(KEY_PASSWORD, encryptedPassword).apply()
    
    return password
}
```

**Quy trình mã hóa password:**
1. Tạo password ngẫu nhiên 32 bytes
2. Mã hóa password bằng Android Keystore (AES/GCM/NoPadding)
3. Lưu password đã mã hóa vào SharedPreferences
4. Khi cần dùng: Giải mã password từ SharedPreferences

**Thông số kỹ thuật:**
- **Algorithm:** AES-256
- **Mode:** GCM (Galois/Counter Mode)
- **Padding:** NoPadding
- **IV Length:** 12 bytes
- **Key Storage:** Android Keystore (hardware-backed nếu thiết bị hỗ trợ)

### 1.4. Dependencies

```kotlin
// build.gradle.kts
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
implementation("androidx.sqlite:sqlite:2.3.1")
```

### 1.5. Lưu Ý Quan Trọng

⚠️ **Migration từ Unencrypted Database:**
- Database hiện tại sử dụng `fallbackToDestructiveMigration(true)`
- Khi enable SQLCipher, database cũ (unencrypted) sẽ bị xóa và tạo mới (encrypted)
- **Dữ liệu cũ sẽ bị mất** khi migrate sang encrypted database

---

## 2. Mã Hóa Dữ Liệu Trong Phiên Làm Việc

### 2.1. Tổng Quan

Dữ liệu nhạy cảm trong phiên làm việc (API keys, location data) được mã hóa trước khi lưu vào DataStore và chỉ giải mã tạm thời khi cần sử dụng.

### 2.2. Kiến Trúc Mã Hóa Phiên

```
┌─────────────────────────────────────────────────────────┐
│  Application Session                                    │
│  ┌───────────────────────────────────────────────────┐ │
│  │ Plaintext Data (API Key, Location, etc.)         │ │
│  │ ↓                                                  │ │
│  │ Encrypt bằng KeyEncryptionManager                │ │
│  │ (AES/GCM với Android Keystore)                   │ │
│  │ ↓                                                  │ │
│  │ Encrypted Data → DataStore (Proto)                │ │
│  │ ↓                                                  │ │
│  │ Khi cần: Decrypt → Plaintext tạm thời            │ │
│  └───────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────┘
```

### 2.3. Cách Triển Khai

#### 2.3.1. KeyEncryptionManager

**File:** `core/data/src/main/java/com/baothanhbin/core/data/encryption/KeyEncryptionManager.kt`

**Chức năng:**
- Mã hóa/giải mã API keys
- Mã hóa/giải mã location data
- Mã hóa/giải mã database password
- Sử dụng Android Keystore với các alias riêng biệt

```kotlin
class KeyEncryptionManager(private val context: Context) {
    companion object {
        private const val KEYSTORE_ALIAS_API_KEY = "api_key_encryption_key"
        private const val KEYSTORE_ALIAS_LOCATION = "location_encryption_key"
        private const val KEYSTORE_ALIAS_DB_PASSWORD = "db_password_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_LENGTH = 12
    }
    
    // Mã hóa API key
    fun encryptApiKey(apiKey: String): String? {
        return encryptString(apiKey, KEYSTORE_ALIAS_API_KEY)
    }
    
    // Giải mã API key
    fun decryptApiKey(encryptedApiKey: String): String? {
        return decryptString(encryptedApiKey, KEYSTORE_ALIAS_API_KEY)
    }
}
```

**Quy trình mã hóa:**
1. Lấy hoặc tạo secret key từ Android Keystore (theo alias)
2. Khởi tạo Cipher với AES/GCM/NoPadding
3. Tạo IV ngẫu nhiên (12 bytes)
4. Mã hóa dữ liệu
5. Kết hợp IV + encrypted data
6. Encode Base64 để lưu trữ

**Quy trình giải mã:**
1. Decode Base64
2. Tách IV (12 bytes đầu) và encrypted data
3. Lấy secret key từ Android Keystore
4. Khởi tạo Cipher với IV
5. Giải mã dữ liệu

### 2.4. Dữ Liệu Được Mã Hóa

- ✅ **API Keys:** Gemini API key
- ✅ **Location Data:** Địa chỉ vị trí người dùng
- ✅ **Database Password:** Password để mở SQLCipher database
- ✅ **Security Settings:** Cài đặt bảo mật (biometric, unlock timestamp)

### 2.5. Bảo Mật

- **Khóa mã hóa:** Lưu trong Android Keystore (hardware-backed)
- **Không thể trích xuất:** Khóa không thể export ra ngoài thiết bị
- **Tách biệt khóa:** Mỗi loại dữ liệu có alias riêng
- **Tạm thời:** Plaintext chỉ tồn tại trong memory khi cần dùng

---

## 3. Bảo Vệ Mã Nguồn

### 3.1. Tổng Quan

Ứng dụng sử dụng **ProGuard/R8** để obfuscate (làm rối) mã nguồn, giúp chống reverse engineering và giảm kích thước APK.

### 3.2. Cấu Hình Build

**File:** `app/build.gradle.kts`

```kotlin
buildTypes {
    release {
        // Bật code obfuscation và minification
        isMinifyEnabled = true
        isShrinkResources = true  // Xóa resources không sử dụng
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
        signingConfig = signingConfigs.getByName("release")
    }
}
```

### 3.3. ProGuard Rules

**File:** `app/proguard-rules.pro`

#### 3.3.1. Package Obfuscation (Repackage Classes)

```proguard
# ============================================
# Package Obfuscation (Repackage all classes)
# ============================================
-repackageclasses 'z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5'
```

**Tác dụng:**
- Đổi tên tất cả package thành tên ngẫu nhiên
- Làm khó đọc code khi decompile
- Giảm kích thước APK

#### 3.3.2. Security Classes Obfuscation

```proguard
# ============================================
# Security & Encryption (Obfuscate everything)
# ============================================
-keepclassmembers,allowobfuscation class com.baothanhbin.core.data.encryption.** {
    <methods>; <fields>;
}
-keepclassmembers,allowobfuscation class com.baothanhbin.agridoctorai.security.SecurityManager {
    <methods>; <fields>;
}
```

**Tác dụng:**
- Cho phép obfuscate các class bảo mật
- Giữ lại cấu trúc class nhưng đổi tên methods/fields
- Khó reverse engineer logic mã hóa

#### 3.3.3. Optimization

```proguard
# ============================================
# Optimization & Minification
# ============================================
-optimizationpasses 5
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
```

**Tác dụng:**
- Tối ưu hóa code qua 5 lần
- Cho phép thay đổi access modifier
- Loại bỏ code không cần thiết

#### 3.3.4. Remove Logging

```proguard
# ============================================
# Remove Logging in Release
# ============================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
```

**Tác dụng:**
- Xóa tất cả Log statements trong release build
- Giảm kích thước APK
- Không để lộ thông tin debug

#### 3.3.5. Rename Source File

```proguard
-renamesourcefileattribute SourceFile
```

**Tác dụng:**
- Ẩn tên file nguồn gốc trong stack trace
- Khó trace về code gốc

### 3.4. Kết Quả Obfuscation

**Trước obfuscation:**
```kotlin
class KeyEncryptionManager {
    fun encryptApiKey(apiKey: String): String? {
        return encryptString(apiKey, KEYSTORE_ALIAS_API_KEY)
    }
}
```

**Sau obfuscation (ví dụ):**
```kotlin
class a {
    fun b(c: String): String? {
        return d(c, "e")
    }
}
```

### 3.5. Lưu Ý

- ✅ **Keep cần thiết:** Một số class phải giữ nguyên tên (Activity, Application, Model classes)
- ✅ **Testing:** Test kỹ release build sau khi obfuscate
- ✅ **Mapping file:** ProGuard tạo mapping.txt để map lại tên gốc (cần bảo vệ file này)

---

## 4. Chống Dịch Ngược Bằng Cách Ký Khi Build

### 4.1. Tổng Quan

Ứng dụng sử dụng **APK Signing** kết hợp với **Signature Verification** để phát hiện và chặn các APK đã bị chỉnh sửa hoặc re-sign.

### 4.2. Cấu Hình Signing

#### 4.2.1. Keystore Properties

**File:** `keystore.properties` (KHÔNG commit lên Git!)

```properties
storePassword=your_store_password
keyPassword=your_key_password
keyAlias=key0
storeFile=path/to/your/keystore.jks
```

#### 4.2.2. Build Configuration

**File:** `app/build.gradle.kts`

```kotlin
// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            if (keystoreProperties.isNotEmpty()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }
    
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### 4.3. Signature Verification

#### 4.3.1. SecurityManager

**File:** `app/src/main/java/com/baothanhbin/agridoctorai/security/SecurityManager.kt`

```kotlin
object SecurityManager {
    private const val EXPECTED_SIGNATURE_SHA256 = "f3036ab303b09bacae8d2c261ab106fffb1bfe1c62ac19748247588ba59b1d393"
    
    /**
     * Thực hiện kiểm tra bảo mật toàn diện
     */
    fun performSecurityCheck(context: Context): SecurityCheckResult {
        val issues = mutableListOf<SecurityIssue>()
        
        if (!isSignatureValid(context)) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.INVALID_SIGNATURE,
                    message = "Chữ ký ứng dụng không hợp lệ. App có thể đã bị chỉnh sửa."
                )
            )
        }
        
        return SecurityCheckResult(
            isSecure = issues.isEmpty(),
            issues = issues
        )
    }
    
    /**
     * Kiểm tra tính hợp lệ của chữ ký ứng dụng
     */
    private fun isSignatureValid(context: Context): Boolean {
        if (isDebuggable(context)) return true  // Bỏ qua trong debug mode
        
        val signatures = getAppSignatures(context) ?: return false
        if (signatures.isEmpty()) return false
        
        val currentSignature = getSignatureSha256(signatures[0])
        return currentSignature == EXPECTED_SIGNATURE_SHA256
    }
    
    /**
     * Tính SHA-256 hash của chữ ký
     */
    private fun getSignatureSha256(signature: Signature): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(signature.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
```

#### 4.3.2. Cách Lấy Signature SHA-256

**Bước 1:** Build release APK với signing config

**Bước 2:** Lấy signature SHA-256 từ APK:

```bash
# Sử dụng keytool
keytool -list -v -keystore your_keystore.jks -alias key0

# Hoặc từ APK đã ký
jarsigner -verify -verbose -certs your_app.apk
```

**Bước 3:** Cập nhật `EXPECTED_SIGNATURE_SHA256` trong `SecurityManager.kt`

