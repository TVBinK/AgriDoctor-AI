# Bảo mật API Key Gemini bằng Android NDK (JNI)

## 📋 Tổng quan

Tài liệu này hướng dẫn cách bảo mật API key của Gemini bằng Android NDK (Native Development Kit), sử dụng JNI (Java Native Interface) để di chuyển API key từ Kotlin/Java code sang native C++ code, làm cho việc reverse engineering trở nên khó khăn hơn nhiều.

## 🎯 Mục tiêu

- Bảo vệ API key khỏi việc bị reverse engineering từ DEX/JAR files
- Tách biệt phần nhạy cảm ra khỏi Kotlin/Java code
- Áp dụng kỹ thuật obfuscation trong native code
- Sử dụng dependency injection (Hilt) để quản lý secret provider

## 🏗️ Kiến trúc

```
┌─────────────────────────────────────────────────────────────┐
│                     ChatbotViewModel                        │
│  (Đã loại bỏ hardcoded API key)                             │ 
└────────────────────────┬────────────────────────────────────┘
                         │ Inject
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   SecretProvider Interface                  │
│  (Abstract interface trong core:network)                    │
└────────────────────────┬────────────────────────────────────┘
                         │ Implement
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                 JniSecretProvider (Kotlin)                  │
│  (JNI wrapper object)                                       │
└────────────────────────┬────────────────────────────────────┘
                         │ JNI Call
                         ▼
┌─────────────────────────────────────────────────────────────┐
│                   secrets.cpp (Native C++)                  │
│  (API key được lưu và obfuscate tại đây)                   │
└─────────────────────────────────────────────────────────────┘
```

## 📦 Các file đã tạo

### 1. Native C++ Code

**File:** `app/src/main/cpp/secrets.cpp`

```cpp
#include <jni.h>
#include <string>
#include <android/log.h>

// Obfuscate API key bằng cách chia thành nhiều phần
static const char KEY_PART1[] = {'A', 'I', 'z', 'a', 'S', 'y', 'B', 'o'};
static const char KEY_PART2[] = {'W', 'j', 'g', 'p', '6', 'z', 's'};
static const char KEY_PART3[] = {'2', 't', 'l', '9', 'Z', 'o', 'n'};
static const char KEY_PART4[] = {'v', 'g', 'P', 'B', 'q', 'Y', 'c'};
static const char KEY_PART5[] = {'s', 'i', 'T', '3', 'q', '1'};
static const char KEY_PART6[] = {'b', 'F', 'e', '4', '\0'};

// Hàm reconstruct API key
static std::string reconstructApiKey() {
    std::string key;
    key.reserve(39);
    key.append(KEY_PART1, sizeof(KEY_PART1));
    key.append(KEY_PART2, sizeof(KEY_PART2));
    key.append(KEY_PART3, sizeof(KEY_PART3));
    key.append(KEY_PART4, sizeof(KEY_PART4));
    key.append(KEY_PART5, sizeof(KEY_PART5));
    key.append(KEY_PART6, sizeof(KEY_PART6));
    return key;
}

// JNI function để lấy API key
extern "C" {
JNIEXPORT jstring JNICALL
Java_com_baothanhbin_agridoctorai_JniSecretProvider_getGeminiApiKey(JNIEnv *env, jobject thiz) {
    std::string apiKey = reconstructApiKey();
    return env->NewStringUTF(apiKey.c_str());
}
}
```

**Đặc điểm:**
- API key được chia nhỏ thành 6 phần để obfuscate
- Chỉ reconstruct khi cần thiết (runtime)
- JNI naming convention: `Java_<package>_<class>_<method>`

### 2. CMakeLists.txt

**File:** `app/src/main/cpp/CMakeLists.txt`

```cmake
cmake_minimum_required(VERSION 3.22.1)

project(secrets)

# Tạo shared library
add_library(
    secrets
    SHARED
    secrets.cpp
)

# Tìm log library
find_library(
    log-lib
    log
)

# Link libraries
target_link_libraries(
    secrets
    ${log-lib}
)
```

### 3. Interface SecretProvider

**File:** `core/network/src/main/java/com/baothanhbin/core/network/SecretProvider.kt`

```kotlin
interface SecretProvider {
    fun getGeminiApiKey(): String
    fun getDebugModelName(): String
    fun getReleaseModelName(): String
}
```

**Mục đích:** Tách biệt abstraction và implementation để dễ test và maintain.

### 4. JNI Wrapper (Kotlin)

**File:** `app/src/main/java/com/baothanhbin/agridoctorai/JniSecretProvider.kt`

```kotlin
object JniSecretProvider : SecretProvider {
    
    init {
        try {
            System.loadLibrary("secrets")
        } catch (e: Exception) {
            android.util.Log.e("JniSecretProvider", "Failed to load native library", e)
        }
    }
    
    override external fun getGeminiApiKey(): String
    override external fun getDebugModelName(): String
    override external fun getReleaseModelName(): String
}
```

**Đặc điểm:**
- Object singleton để load native library
- External functions được link với native code
- Load library trong init block

### 5. Hilt Module

**File:** `app/src/main/java/com/baothanhbin/agridoctorai/di/AppModule.kt`

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideSecretProvider(): SecretProvider {
        return JniSecretProvider
    }
}
```

**Mục đích:** Provide SecretProvider cho dependency injection.

### 6. Build Configuration

**File:** `app/build.gradle.kts`

```kotlin
android {
    defaultConfig {
        // NDK configuration
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
                arguments += "-DANDROID_STL=c++_shared"
            }
        }
    }
    
    // External native build configuration
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // Thêm dependency
    implementation(project(":core:network"))
    // ... other dependencies
}
```

### 7. Cập nhật ChatbotViewModel

**File:** `feature/chatbot/src/main/java/com/baothanhbin/feature/chatbot/ChatbotViewModel.kt`

**Trước:**
```kotlin
private val API_KEY =
    if (BuildConfig.BUILD_TYPE == "debug") "REMOVED" else "REMOVED"
```

**Sau:**
```kotlin
@HiltViewModel
class ChatbotViewModel @Inject constructor(
    application: Application,
    private val database: AgriDoctorDatabase,
    private val secretProvider: SecretProvider
) : AndroidViewModel(application) {
    
    private val MODEL_NAME =
        if (BuildConfig.BUILD_TYPE == "debug") 
            secretProvider.getDebugModelName() 
        else 
            secretProvider.getReleaseModelName()
    
    private val API_KEY = secretProvider.getGeminiApiKey()
}
```

## 🛠️ Các bước thực hiện

### Bước 1: Tạo native code structure

```bash
mkdir -p app/src/main/cpp
```

### Bước 2: Tạo secrets.cpp với obfuscated API key

- Chia API key thành nhiều phần
- Tạo hàm reconstruct ở runtime
- Implement JNI functions theo naming convention

### Bước 3: Tạo CMakeLists.txt

- Configure CMake build cho native library
- Link với Android log library
- Set C++ standard version

### Bước 4: Tạo SecretProvider interface

- Tạo trong core:network module
- Define abstract methods cho secrets

### Bước 5: Tạo JniSecretProvider wrapper

- Implement SecretProvider interface
- Load native library trong init block
- Declare external functions

### Bước 6: Cấu hình build.gradle.kts

- Thêm NDK configuration
- Configure external native build
- Add CMakeLists.txt path
- Thêm dependency :core:network

### Bước 7: Tạo Hilt AppModule

- Provide SecretProvider implementation
- Use Singleton scope

### Bước 8: Cập nhật ChatbotViewModel

- Inject SecretProvider thay vì hardcode
- Remove API key khỏi Kotlin code

## ✅ Kết quả

### Trước khi bảo mật:
- API key hiển thị rõ trong Kotlin code
- Dễ dàng extract từ DEX/JAR files
- Có thể decompile và tìm thấy bằng text search

### Sau khi bảo mật:
- ✅ API key chỉ tồn tại trong native binary
- ✅ Obfuscated thành nhiều phần
- ✅ Chỉ reconstruct ở runtime
- ✅ Không thể tìm thấy trong Kotlin/DEX code
- ✅ Gây khó khăn cho reverse engineering

## 🔒 Lợi ích bảo mật

1. **Bảo vệ khỏi decompilation:** Native code khó decompile hơn Java/Kotlin
2. **Obfuscation:** Chia API key thành nhiều phần
3. **Runtime reconstruction:** Chỉ ghép lại khi cần
4. **Tách biệt concerns:** Secret logic tách khỏi business logic
5. **Dễ maintain:** Có thể swap implementation (NDK vs obfuscation vs encryption)

## 📝 Lưu ý

### Những gì KHÔNG thay thế:
- Không bảo vệ 100% - vẫn có thể reverse với đủ thời gian và kỹ năng
- Không che giấu hoạt động runtime của app
- Không bảo vệ khỏi man-in-the-middle attacks

### Những gì NÊN làm thêm:
1. **API Key Rotation:** Thay đổi key định kỳ
2. **Rate Limiting:** Thêm rate limiting ở server
3. **App Authentication:** Verify app identity
4. **Server-side Proxy:** Route requests qua backend server
5. **Additional Encryption:** Layer thêm encryption cho network requests

## 🧪 Testing

### Build và run:
```bash
./gradlew :app:assembleDebug
```

### Verify:
- Check build logs để ensure native library được compile
- Test app functionality để ensure API key được load đúng
- Check native library có trong APK:
  ```bash
  unzip app-debug.apk lib/*.so
  ```

## 📚 Tài liệu tham khảo

- [Android NDK Documentation](https://developer.android.com/ndk)
- [JNI Specification](https://docs.oracle.com/javase/8/docs/technotes/guides/jni/)
- [CMake Documentation](https://cmake.org/documentation/)
- [Dagger Hilt Documentation](https://dagger.dev/hilt/)

## 🎓 Các kỹ thuật bảo mật khác có thể áp dụng

1. **String Obfuscation:** Encode strings trong compiled code
2. **Control Flow Flattening:** Làm code flow phức tạp hơn
3. **White-box Cryptography:** Encrypt tại compile time
4. **Dynamic Library Loading:** Load code từ server
5. **Root Detection:** Detect rooted devices và block

---

**Tác giả:** AI Assistant  
**Ngày:** 2025  
**Version:** 1.0

