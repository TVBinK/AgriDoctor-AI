# Hướng dẫn cấu hình mạng cho AgriDoctor AI

## 📱 Kết nối với Server API

### ✅ Thiết bị thật (KHÔNG CẦN CẮM CÁP USB)

**Yêu cầu:**
1. ✅ Thiết bị Android và máy chạy server **cùng mạng WiFi**
2. ✅ Biết IP của máy chạy server
3. ✅ Server đang chạy trên port 3443

**Các bước:**

#### Bước 1: Lấy IP máy chạy server

**Windows:**
```bash
# Mở Command Prompt (cmd)
ipconfig

# Tìm "IPv4 Address" trong mục WiFi hoặc Ethernet
# Ví dụ: 192.168.1.100
```

**Mac/Linux:**
```bash
# Mở Terminal
ifconfig
# hoặc
ip addr

# Tìm IP trong mục wlan0 hoặc en0
# Ví dụ: 192.168.1.100
```

#### Bước 2: Cập nhật IP trong code

Mở file: `core/network/src/main/java/com/baothanhbin/core/network/NetworkClients.kt`

Thay đổi IP tại dòng 95 và 101:
```kotlin
val detectClient: HttpClient = createClient(
    baseUrl = "https://192.168.1.100:3443/api/detect", // ← Thay IP ở đây
    acceptAllCertificates = BuildConfig.DEBUG
)

val apiKeyClient: HttpClient = createClient(
    baseUrl = "https://192.168.1.100:3443/api", // ← Thay IP ở đây
    acceptAllCertificates = BuildConfig.DEBUG
)
```

#### Bước 3: Đảm bảo cùng mạng WiFi

- Thiết bị Android: Kết nối WiFi (ví dụ: "Home_WiFi")
- Máy chạy server: Cùng WiFi "Home_WiFi"
- **KHÔNG CẦN cắm cáp USB!**

#### Bước 4: Kiểm tra kết nối

**Trên máy chạy server:**
```bash
# Test server có chạy không
curl https://localhost:3443/api/gemini-key
```

**Trên thiết bị Android:**
- Mở trình duyệt Chrome
- Truy cập: `https://[IP_MÁY]:3443/api/gemini-key`
- Nếu thấy response → Kết nối thành công!

### ✅ Emulator (tự động)

- Emulator tự động dùng `10.0.2.2` để truy cập localhost của máy
- Không cần cấu hình gì thêm
- Server chạy trên máy → Emulator tự kết nối được

### ⚠️ Lưu ý quan trọng

1. **IP có thể thay đổi**
   - Mỗi lần đổi WiFi, IP có thể thay đổi
   - Cần cập nhật lại IP trong code

2. **Firewall**
   - Đảm bảo firewall không chặn port 3443
   - Windows: Cho phép port 3443 trong Windows Firewall
   - Mac/Linux: Kiểm tra firewall settings

3. **Server phải đang chạy**
   - Server phải đang listen trên port 3443
   - Kiểm tra: `netstat -an | grep 3443` (Mac/Linux)
   - Hoặc: `netstat -an | findstr 3443` (Windows)

4. **HTTPS với self-signed certificate**
   - App đã được cấu hình để chấp nhận self-signed cert
   - Chỉ trong debug mode
   - Production cần certificate hợp lệ

### 🔧 Troubleshooting

**Lỗi: "Unable to connect"**
- ✅ Kiểm tra thiết bị và máy cùng WiFi
- ✅ Kiểm tra IP đúng chưa
- ✅ Kiểm tra server có đang chạy không
- ✅ Kiểm tra firewall

**Lỗi: "Certificate error"**
- ✅ App đã cấu hình accept self-signed trong debug mode
- ✅ Nếu vẫn lỗi, kiểm tra network_security_config.xml

**Lỗi: "Connection timeout"**
- ✅ Kiểm tra server có listen trên port 3443 không
- ✅ Kiểm tra IP và port đúng chưa
- ✅ Thử ping IP từ thiết bị Android

### 📝 Tóm tắt

| Thiết bị | Cần cáp USB? | IP cần dùng | Ghi chú |
|----------|--------------|-------------|---------|
| **Thiết bị thật** | ❌ **KHÔNG** | IP máy (vd: 192.168.1.100) | Phải cùng WiFi |
| **Emulator** | ❌ Không | 10.0.2.2 | Tự động |

**Kết luận: KHÔNG CẦN CẮM CÁP USB! Chỉ cần cùng WiFi là đủ! 🎉**

