<p align="center">
  <img src="resources/src/main/res/drawable-xxxhdpi/ic_launcher_foreground.webp" alt="AgriDoctorAI" width="96" />
</p>

# AgriDoctorAI

AgriDoctorAI là trợ lý canh tác số giúp người nông dân phát hiện bệnh cây, tư vấn xử lý và quản lý nhật ký canh tác ngay trên thiết bị Android. Ứng dụng kết hợp AI thị giác máy tính, chatbot ngôn ngữ và cảm biến ánh sáng để cung cấp quyết định chính xác, nhanh chóng và an toàn.

---

## Nội dung chính

1. [Tính năng nổi bật](#tính-năng-nổi-bật)  
2. [Kiến trúc & Module](#kiến-trúc--module)  
3. [Yêu cầu môi trường](#yêu-cầu-môi-trường)  
4. [Hướng dẫn thiết lập](#hướng-dẫn-thiết-lập)  
5. [Quy trình build & chạy](#quy-trình-build--chạy)  
6. [Bảo mật & chống giả mạo](#bảo-mật--chống-giả-mạo)  
7. [Tài liệu bổ sung](#tài-liệu-bổ-sung)

---

## Tính năng nổi bật

- **Chẩn đoán bệnh cây**: Nhận diện bệnh qua ảnh chụp trong `feature/camera` và trả kết quả tại `feature/diagnoseresult`.
- **Chatbot nông nghiệp**: Tương tác Gemini API để đặt câu hỏi tự nhiên trong `feature/chatbot`.
- **Gợi ý chăm sóc**: Lưu nhật ký, theo dõi tiến độ qua `feature/myplants` và `feature/home`.
- **Đo sáng và nhắc tưới**: `feature/lightmeter` hỗ trợ đo cường độ ánh sáng, khuyến nghị tưới tiêu.
- **Pipeline xử lý ảnh**: Module `feature/processimage` kết hợp `core/model` và `core/network` cho inference nội bộ.
- **Bảo mật nhiều lớp**: Mã hóa API key, kiểm tra chữ ký, obfuscation R8 và cảnh báo khi ứng dụng bị chỉnh sửa.

---

## Kiến trúc & Module

```
app/                 : Application module + DI Hilt
core/
  data/              : Repository + DataStore/API source
  database/          : SQLCipher + DAO
  datastore/         : Proto DataStore cho cấu hình bảo mật
  model/             : Entity & mapper dùng chung
  network/           : Retrofit/Ktor client
  theme/ ui/         : Design system + components
feature/
  camera/ diagnose/  : Flow chẩn đoán
  chatbot/           : Gemini assistant
  processimage/      : Pipeline xử lý hình ảnh
  ...
resources/           : Assets, icon, font
build-logic/         : Gradle convention plugins
```

Ứng dụng tuân thủ Clean Architecture: `feature` (UI + ViewModel) phụ thuộc `core` (data/domain) thông qua interface repository được bind bởi Hilt.

---

## Yêu cầu môi trường

- **Android Studio** Iguana trở lên với JDK 17.
- **Gradle** wrapper đi kèm repo (`./gradlew`).
- **Android SDK** 34, NDK (nếu build mô-đun C++).
- **Gemini API Key**: tạo tại Google AI Studio, lưu bằng màn hình thiết lập trong app.
- **Keystore** riêng cho build release (tham khảo [Bảo mật](#bảo-mật--chống-giả-mạo)).

---

## Hướng dẫn thiết lập

1. **Clone dự án**
   ```bash
   git clone <repo-url>
   cd AgriDoctorAI
   ```
2. **Đồng bộ Android Studio**  
   Mở thư mục root, chờ Gradle sync hoàn tất.
3. **Cập nhật tệp cấu hình**  
   - `local.properties`: chỉ đường dẫn `sdk.dir`, `ndk.dir`.  
   - `keystore.properties`: nếu build release.
4. **Nhập API Key**  
   Vào màn hình `Cài đặt` trong app, nhập Gemini API Key để dùng chatbot.

---

## Quy trình build & chạy

- **Build debug nhanh**
  ```bash
  ./gradlew :app:assembleDebug
  ```
- **Chạy instrumentation test**
  ```bash
  ./gradlew :app:connectedDebugAndroidTest
  ```
- **Phát hành release** (cần keystore)
  ```bash
  ./gradlew :app:bundleRelease
  ```
- **Cài APK đã chỉnh sửa** (khi cần kiểm thử bảo mật)
  ``` 
  "C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe" install "D:\Data\Android\APK\modified\app_modified.apk"
  ```
- **Cách setup SHA 256**
  ```
  "C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "D:\Data\Android\APK\key_debug_by_bin" -alias key
  ```

---

## Bảo mật & chống giả mạo

### 1. Kiểm tra SHA-256 chữ ký

```
"C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" ^
  -list -v -keystore "D:\Data\Android\APK\key_debug_by_bin" -alias key0
```

- Hash SHA-256 được hardcode trong `SecurityManager`.
- Khi app khởi chạy:
  1. Đọc certificate từ APK cài đặt.
  2. Tính SHA-256 runtime.
  3. So sánh với hash kỳ vọng.
  4. Khác biệt ⇒ cảnh báo app bị re-sign.

### 2. Mã hóa database với SQLCipher + Android Keystore

- Room database trong `core/database` được mã hóa bằng **SQLCipher**.
- Password database là **32 bytes random** sinh bằng `SecureRandom` (256-bit), được:
  - Encode Base64.
  - **Mã hóa bằng `KeyEncryptionManager` (AES/GCM, Android Keystore, alias riêng cho DB)**.
  - Lưu vào `SharedPreferences` dưới dạng ciphertext (`database_password_prefs.xml`), đã exclude khỏi backup.
- Khi app khởi chạy:
  1. Đọc ciphertext password từ SharedPreferences.
  2. Giải mã bằng Android Keystore để lấy lại Base64 gốc.
  3. Decode thành `ByteArray` và truyền vào `SQLCipher SupportFactory` cho Room.
- Key AES trong Keystore **không thể export** ra ngoài app, giúp giảm rủi ro nếu chỉ bị lộ file DB + SharedPreferences.

### 3. Bảo vệ API Key Gemini

- API key được mã hóa AES-256 với khóa sinh từ Android Keystore (hardware-backed).  
- Ciphertext lưu trong Proto DataStore (`core/datastore`).  
- Khóa giải mã không thể trích xuất khỏi thiết bị.  
- ViewModel chỉ nhận plaintext tạm thời khi gửi request.

```kotlin
val encrypted = keyEncryptionManager.encrypt(apiKey)
dataStore.save(encrypted)

val cipher = dataStore.read()
val plain = keyEncryptionManager.decrypt(cipher)
```

### 4. Obfuscation bằng R8/ProGuard

```proguard
-repackageclasses 'z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5'
-keepclassmembers,allowobfuscation class com.baothanhbin.agridoctorai.security.SecurityManager {
    <methods>;
}
```

- Đổi tên class/method, xóa code thừa, khó đọc khi decompile.
- Kết hợp shrink + optimize giúp APK nhỏ gọn hơn.

### 5. Phát hiện APK bị chỉnh sửa

Khi hacker recompile & re-sign:

```
Signature gốc: f3036ab303b09b...
Signature mod:  a1b2c3d4e5f6...
```

Tại runtime, `SecurityManager.performSecurityCheck()` phát hiện chữ ký mới không khớp, khóa các tính năng quan trọng và hiển thị cảnh báo “Ứng dụng đã bị chỉnh sửa bất hợp pháp”.

---

## Tài liệu bổ sung

- `core/database/SQLCIPHER_SETUP.md`: hướng dẫn cấu hình SQLCipher.  
- `build-logic/convention`: custom Gradle plugin dùng chung.  
- `feature/*/README.md` (nếu có): mô tả chi tiết từng flow.  
- Báo cáo lỗi hoặc đề xuất tính năng qua Issues/Pull Request.

---

Chúc bạn có trải nghiệm canh tác thông minh cùng AgriDoctorAI! Mọi đóng góp, câu hỏi xin gửi về đội ngũ phát triển qua GitHub hoặc email nội bộ.

