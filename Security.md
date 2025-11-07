# 🔒 TỔNG KẾT - HỆ THỐNG BẢO MẬT APP

## Cách setup SHA256 
"C:\Program Files\Android\Android Studio\jbr\bin\keytool.exe" -list -v -keystore "D:\Data\Android\APK\key_debug_by_bin" -alias key0
## Cách cài ứng dụng đã modified
"C:\Users\Admin\AppData\Local\Android\Sdk\platform-tools\adb.exe" install "D:\Data\Android\APK\modified\app_modified.apk"

---

## 🛡️  LỚP BẢO VỆ ĐÃ TRIỂN KHAI:
 ✅ **Signature Verification** - Kiểm tra chữ ký app
   - SHA-256: `f3036ab303b09bacae8d2c261ab106fffb1bfe1c62ac19748247588ba59b1d393`
   - Keystore: `D:/Data/Android/APK/key_debug_by_bin`
   - Phát hiện khi app bị re-sign bằng keystore khác
