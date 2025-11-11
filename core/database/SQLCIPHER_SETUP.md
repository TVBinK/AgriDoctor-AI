# 🔒 SQLCipher Database Encryption Setup

## Tổng Quan

Database đã được mã hóa bằng **SQLCipher** để bảo vệ dữ liệu nhạy cảm như:
- Location data (địa chỉ vị trí)
- Image URIs
- Diagnosis results
- Timestamps

## Cách Hoạt Động

### 1. **Database Password Management**
- Password database (32 bytes) được generate ngẫu nhiên bằng `SecureRandom`
- Password được mã hóa bằng `KeyEncryptionManager` (sử dụng Android Keystore)
- Password đã mã hóa được lưu trong `SharedPreferences`

### 2. **Encryption Flow**
```
[Generate Random Password]
    ↓
[Encrypt với Android Keystore (AES-256)]
    ↓
[Lưu encrypted password vào SharedPreferences]
    ↓
[Khi cần dùng: Decrypt password]
    ↓
[Sử dụng password để mở SQLCipher database]
```

### 3. **Security Features**
- ✅ Database được mã hóa AES-256
- ✅ Password được lưu encrypted (không thể đọc plaintext)
- ✅ Password key nằm trong Android Keystore (hardware-backed)
- ✅ Không thể truy cập database nếu không có password

## Cấu Trúc Code

### **DatabasePasswordManager**
- `getDatabasePassword()`: Lấy password (tạo mới nếu chưa có)
- `generateAndSavePassword()`: Tạo và lưu password mới
- `clearPassword()`: Xóa password (dùng khi reset database)

### **DatabaseModule**
- Sử dụng `SupportFactory` từ SQLCipher để mã hóa database
- Password được lấy từ `DatabasePasswordManager`
- Database được mã hóa tự động khi tạo

## Dependencies

```kotlin
implementation("net.zetetic:android-database-sqlcipher:4.5.4")
implementation("androidx.sqlite:sqlite:2.3.1")
```

## Migration từ Unencrypted Database

⚠️ **LƯU Ý QUAN TRỌNG:**
- Database hiện tại sử dụng `fallbackToDestructiveMigration(true)`
- Khi enable SQLCipher, database cũ (unencrypted) sẽ bị xóa và tạo mới (encrypted)
- **Dữ liệu cũ sẽ bị mất** khi migrate sang encrypted database

### Nếu cần giữ dữ liệu cũ:
1. Export dữ liệu từ database cũ
2. Enable SQLCipher (database mới được tạo)
3. Import dữ liệu vào database mới (encrypted)

## Testing

### Kiểm tra database đã được mã hóa:
```bash
# Thử mở database bằng SQLite browser thông thường
# Nếu database đã được mã hóa, sẽ không thể đọc được
```

### Kiểm tra password được lưu an toàn:
```kotlin
// Password trong SharedPreferences phải là encrypted string
// Không thể đọc được plaintext password
```

## Troubleshooting

### Lỗi: "Database is encrypted or is not a database"
- **Nguyên nhân**: Database đã được mã hóa nhưng password không đúng
- **Giải pháp**: Xóa app và reinstall (password mới sẽ được generate)

### Lỗi: "Failed to decrypt database password"
- **Nguyên nhân**: Android Keystore key bị mất hoặc thay đổi
- **Giải pháp**: Clear password và generate lại (dữ liệu cũ sẽ bị mất)

## Best Practices

1. **Backup Exclusion**: Database không nên được backup lên cloud
   - Thêm vào `backup_rules.xml`:
   ```xml
   <exclude domain="database" path="agridoctor_database" />
   ```

2. **Password Rotation**: Có thể implement password rotation nếu cần
   - Export dữ liệu
   - Generate password mới
   - Import dữ liệu vào database mới

3. **Data Retention**: Xóa dữ liệu cũ sau thời gian nhất định
   - Giảm rủi ro nếu database bị leak
   - Tuân thủ privacy regulations

## Performance

- **Overhead**: SQLCipher có overhead nhỏ (~5-10%) so với SQLite thông thường
- **Encryption**: Tự động mã hóa/giải mã khi đọc/ghi
- **Impact**: Không đáng kể với database nhỏ (< 100MB)

## References

- [SQLCipher Documentation](https://www.zetetic.net/sqlcipher/)
- [Room with SQLCipher](https://www.zetetic.net/sqlcipher/sqlcipher-for-android/)
- [Android Keystore](https://developer.android.com/training/articles/keystore)

