# 🛡️ NGUYÊN LÝ HOẠT ĐỘNG 3 LỚP BẢO MẬT

Dùng keystore (chứa private key) để KÝ APK, rồi lấy SHA-256 là hash CỦA signature đó để SO SÁNH sau này.

# Keystore chứa Key tạo bằng RSA
Nguyên lý RSA:
Ký bằng Private Key (chỉ bạn có)
Verify bằng Public Key (ai cũng có)
Nếu verify thành công → Chứng minh được ký bởi Private Key tương ứng

1. Hardcode SHA-256 của Certificate gốc vào code
   ↓
2. Khi app chạy:
   - Đọc Certificate từ APK đã cài
   - Tính SHA-256 của Certificate đó
   - So sánh với SHA-256 hardcoded
   ↓
3. Nếu khác → Certificate khác → Keystore khác → Re-signed!
   ↓
4. Hiển thị cảnh báo

## 1️⃣ BẢO MẬT API KEY GEMINI (Encryption + Secure Storage)

### **Nguyên lý:**
Mã hóa API key trước khi lưu, giải mã khi sử dụng.

### **Cách hoạt động:**

```
[API Key Gốc] 
    ↓
[Mã hóa AES-256 + Android Keystore]
    ↓
[Lưu vào DataStore (encrypted)]
    ↓
Khi cần dùng:
    ↓
[Đọc từ DataStore] → [Giải mã] → [Sử dụng]
```

### **Chi tiết:**

1. **Khi lưu API key:**
   - User nhập API key
   - App tạo encryption key từ Android Keystore (phần cứng)
   - Mã hóa API key bằng AES-256
   - Lưu ciphertext vào DataStore

2. **Khi sử dụng API key:**
   - Đọc ciphertext từ DataStore
   - Lấy encryption key từ Keystore
   - Giải mã thành plaintext
   - Gửi request với API key

3. **Bảo vệ:**
   - ✅ API key KHÔNG lưu plaintext
   - ✅ Encryption key nằm trong hardware Keystore (không thể extract)
   - ✅ Hacker decompile chỉ thấy ciphertext vô nghĩa
   - ✅ Không thể giải mã nếu không có device key

### **Code:**
```kotlin
// Mã hóa
val encrypted = keyEncryptionManager.encrypt(apiKey)
dataStore.save(encrypted) // Lưu ciphertext

// Giải mã
val encrypted = dataStore.read()
val apiKey = keyEncryptionManager.decrypt(encrypted)
```

---

## 2️⃣ PROGUARD R8 OBFUSCATION (Làm Rối Mã Nguồn)

### **Nguyên lý:**
Biến đổi code thành dạng khó đọc nhưng vẫn chạy được.

### **Cách hoạt động:**

```
[Code Gốc - Dễ Đọc]
    ↓
[R8 Compiler]
    ↓
• Đổi tên class: ChatbotViewModel → a
• Đổi tên method: sendMessage() → b()
• Đổi tên package: com.baothanhbin.feature.chatbot → z9x8w7...a5
• Xóa code không dùng (shrinking)
• Tối ưu hóa bytecode (optimization)
    ↓
[APK - Code Rối Rắm, Khó Đọc]
```

### **Ví dụ:**

**Trước obfuscation:**
```kotlin
package com.baothanhbin.agridoctorai.security

class SecurityManager {
    fun performSecurityCheck(context: Context): SecurityCheckResult {
        if (!isSignatureValid(context)) {
            return SecurityCheckResult(isSecure = false)
        }
        return SecurityCheckResult(isSecure = true)
    }
    
    private fun isSignatureValid(context: Context): Boolean {
        val signature = getSignature(context)
        return signature == EXPECTED_SIGNATURE
    }
}
```

**Sau obfuscation (khi decompile):**
```java
package z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5;

public class a {
    public b a(Context c) {
        if (!b(c)) {
            return new b(false);
        }
        return new b(true);
    }
    
    private boolean b(Context c) {
        String s = c(c);
        return s.equals(d);
    }
}
```

### **Bảo vệ:**
- ✅ Hacker không biết class nào là SecurityManager
- ✅ Không biết method nào kiểm tra signature
- ✅ Phải đọc toàn bộ code để tìm logic
- ✅ Tốn nhiều thời gian (giờ → ngày)

### **ProGuard Rules:**
```proguard
# Obfuscate tất cả vào package ngẫu nhiên
-repackageclasses 'z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5'

# Cho phép đổi tên SecurityManager
-keepclassmembers,allowobfuscation class com.baothanhbin.agridoctorai.security.SecurityManager {
    <methods>;
}
```

---

## 3️⃣ CHỐNG DECOMPILE APP (Signature + Installer Check)

### **Nguyên lý:**
Phát hiện khi app bị decompile và recompile.

### **Cách hoạt động:**

```
[Khi Build APK Gốc]
    ↓
APK được ký bằng keystore của developer
    ↓
Signature SHA-256: f3036ab303b09b...
    ↓
Lưu SHA-256 vào SecurityManager (hardcode)

---

[Khi Hacker Decompile]
    ↓
Hacker dùng apktool decompile APK
    ↓
Chỉnh sửa code (thêm ads, malware...)
    ↓
Recompile APK
    ↓
PHẢI re-sign bằng keystore khác (không có keystore gốc)
    ↓
Signature SHA-256 mới: a1b2c3d4e5f6... (KHÁC!)

---

[Khi User Mở App Mod]
    ↓
SecurityManager.performSecurityCheck()
    ↓
Lấy signature hiện tại: a1b2c3d4e5f6...
    ↓
So sánh với SHA-256 gốc: f3036ab303b09b...
    ↓
KHÔNG KHỚP! → Phát hiện app bị chỉnh sửa
    ↓
Hiển thị cảnh báo: "App đã bị chỉnh sửa bất hợp pháp"
```

### **Chi tiết 2 kiểm tra:**

#### **A. Signature Verification:**
```kotlin
// Lấy signature của APK hiện tại
val currentSignature = getAppSignature(context)
val currentSHA256 = sha256(currentSignature)

// So sánh với SHA-256 gốc (hardcode)
if (currentSHA256 != EXPECTED_SIGNATURE_SHA256) {
    // App bị re-sign → Cảnh báo!
    return false
}
```

**Tại sao phát hiện được:**
- APK gốc: Ký bằng keystore của bạn → SHA-256 = `f3036ab3...`
- APK mod: Ký bằng keystore hacker → SHA-256 = `a1b2c3d4...`
- SHA-256 khác nhau → Phát hiện ngay!

