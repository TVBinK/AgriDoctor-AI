# 🌾 AgriDoctorAI

<p align="center">
  <img src="https://img.shields.io/badge/Android-Kotlin-3DDC84?style=flat&logo=android&logoColor=white" alt="Android Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack-Compose-4285F4?style=flat&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose"/>
  <img src="https://img.shields.io/badge/Hilt-DI-FF6F00?style=flat" alt="Hilt"/>
  <img src="https://img.shields.io/badge/Room-Database-4285F4?style=flat" alt="Room Database"/>
</p>

Ứng dụng Android giúp **chẩn đoán bệnh cây bằng AI, tư vấn chăm sóc và quản lý cây trồng** cho nông dân ngay trên điện thoại. Xây dựng bằng Jetpack Compose, kiến trúc đa module với Hilt, Room, WorkManager và tích hợp Gemini cho trợ lý hội thoại.

<p align="center">
  <img src="resources/src/main/res/drawable/banner_readme.png" alt="AgriDoctorAI Banner" width="70%"/>
</p>

---

## 📋 Mục lục

- [Tổng quan](#-tổng-quan)
- [Chức năng chi tiết](#-chức-năng-chi-tiết)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)

---

## 🎯 Tổng quan

|             |                                                                 |
|------------|-----------------------------------------------------------------|
| **Nền tảng** | Android (Kotlin)                                               |
| **UI**       | Jetpack Compose                                                |
| **Kiến trúc**| Multi-module, MVVM, Clean Architecture                         |
| **DI**       | Hilt                                                           |
| **Database** | Room                                                           |
| **AI**       | Gemini API (chatbot, hỗ trợ chẩn đoán)                        |
| **Background** | WorkManager (xử lý nền, tác vụ lâu)                         |

---

## 🔧 Chức năng chi tiết

### 🌿 1. Chẩn đoán bệnh cây (Diagnose)

| Icon | Mô tả |
|:---:|---|
| 📷 | **Chụp ảnh lá/cây trồng** – Sử dụng camera trong `feature/camera` để chụp ảnh mẫu bệnh |
| 🧪 | **Phân tích bệnh bằng AI** – Gửi ảnh qua pipeline xử lý trong `feature/processimage` + `core/model` |
| 📊 | **Kết quả chẩn đoán chi tiết** – Hiển thị tại `feature/diagnoseresult` với tên bệnh, mức độ, nguyên nhân |
| 💊 | **Gợi ý điều trị** – Đề xuất thuốc, liều lượng, quy trình xử lý an toàn |

**Cách hoạt động:**
- Người dùng chụp ảnh cây bị bệnh hoặc chọn ảnh có sẵn (nếu hỗ trợ).
- Ảnh được xử lý (resize, chuẩn hóa) trong `feature/processimage`.
- Gửi đến mô hình AI / backend qua `core/network`.
- Kết quả trả về được map sang model hiển thị trong `feature/diagnoseresult`.

---

### 🤖 2. Chatbot nông nghiệp (AI Assistant)

| Icon | Mô tả |
|:---:|---|
| 💬 | Đặt câu hỏi tự nhiên về kỹ thuật trồng trọt, phòng trừ sâu bệnh |
| 🌱 | Gợi ý giống, lịch gieo trồng, phân bón phù hợp từng loại cây |
| 🔁 | Lưu và tiếp tục lịch sử hội thoại (nếu được bật) |

**Cách hoạt động:**
- Giao diện hội thoại được xây dựng trong `feature/chatbot`.
- Mỗi tin nhắn được gửi tới Gemini API thông qua `core/network`.
- API key và cấu hình được lưu trong `core/datastore`.
- Kết quả được stream về UI Compose, hiển thị tương tự ứng dụng chat.

---

### 📒 3. Quản lý cây trồng & nhật ký (My Plants)

| Icon | Mô tả |
|:---:|---|
| 🌿 | Thêm cây trồng, giống, diện tích, ngày gieo trồng |
| 🗓️ | Ghi chép nhật ký canh tác: bón phân, tưới, phun thuốc, thu hoạch |
| 📈 | Theo dõi tiến độ sinh trưởng, năng suất ước tính |

**Cách hoạt động:**
- Dữ liệu cây trồng lưu trong Room tại `core/database`.
- UI chính được tổ chức trong `feature/home` và `feature/myplants`.
- ViewModel dùng repository từ `core/data` để thao tác dữ liệu một cách tách biệt với UI.

---

### ☀️ 4. Đo sáng & nhắc tưới (Light Meter)

| Icon | Mô tả |
|:---:|---|
| ☀️ | Đo cường độ ánh sáng tại vị trí cây trồng |
| 📉 | So sánh với ngưỡng tối ưu từng loại cây |
| ⏰ | Gợi ý thời điểm tưới nước hoặc che nắng/bổ sung ánh sáng |

**Cách hoạt động:**
- Module `feature/lightmeter` đọc cảm biến ánh sáng / camera (tùy cấu hình).
- Giá trị đo được chuẩn hóa, so với cấu hình khuyến nghị trong `core/model`.
- WorkManager có thể được dùng để đặt lịch nhắc tưới hoặc kiểm tra định kỳ.

---

### 🔐 5. Quyền truy cập (Require Permission)

| Icon | Mô tả |
|:---:|---|
| 📷 | Yêu cầu quyền Camera để chụp ảnh chẩn đoán |
| 📂 | Yêu cầu quyền truy cập Media/Storage (nếu có chức năng chọn ảnh) |
| 📡 | Quyền Internet để gọi API Gemini / backend |

**Cách hoạt động:**
- Màn hình yêu cầu quyền có thể được tổ chức trong `feature/requirepermission` hoặc flow khởi động app.
- Chỉ khi người dùng cấp quyền cần thiết, ứng dụng mới cho phép truy cập đầy đủ các tính năng chẩn đoán và chatbot.

---

## 📁 Cấu trúc dự án

```text
├── app/                    # App chính, Hilt setup, navigation root
├── core/
│   ├── theme/              # Giao diện, màu sắc, typography
│   ├── ui/                 # UI components dùng chung (Compose)
│   ├── model/              # Data models, mapper dùng chung
│   ├── network/            # Retrofit/Ktor client, Gemini API
│   ├── data/               # Repository implementations
│   ├── database/           # Room entities, DAOs
│   └── datastore/          # DataStore (Proto/Preferences) cho cấu hình app
├── feature/
│   ├── camera/             # Chụp ảnh lá, chuẩn bị dữ liệu chẩn đoán
│   ├── diagnose/           # Luồng chẩn đoán bệnh cây
│   ├── diagnoseresult/     # Kết quả chẩn đoán & gợi ý xử lý
│   ├── chatbot/            # Chatbot nông nghiệp dùng Gemini
│   ├── myplants/           # Danh sách cây, nhật ký canh tác
│   ├── home/               # Màn hình tổng quan, entry point chính
│   ├── lightmeter/         # Đo sáng, gợi ý tưới & chiếu sáng
│   ├── processimage/       # Xử lý ảnh trước khi gửi AI
│   ├── settings/           # Cài đặt app và API key
│   └── requirepermission/  # Màn hình yêu cầu quyền (nếu có)
├── resources/              # Drawables, strings, fonts
├── build-logic/            # Gradle convention plugins dùng chung
└── simple-navigation/      # Navigation module (Compose Navigation)
```

Ứng dụng tuân thủ Clean Architecture: các module `feature` (UI + ViewModel) chỉ phụ thuộc vào `core` thông qua interface repository, tất cả được bind bằng Hilt để dễ test và mở rộng.

---

## 🚀 Công nghệ sử dụng

- **Kotlin** · **Jetpack Compose** · **MVVM** · **Clean Architecture**
- **Hilt** · **Room** ·
- **Ktor** · **Coroutines/Flow** · **Navigation Compose** · **Coil**
- **DataStore** ·

---