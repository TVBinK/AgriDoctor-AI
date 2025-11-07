# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ============================================
# Kotlin
# ============================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ============================================
# Kotlin Coroutines
# ============================================
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.**

# ============================================
# Kotlin Serialization
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.baothanhbin.core.model.**$$serializer { *; }
-keepclassmembers class com.baothanhbin.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.baothanhbin.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ============================================
# Hilt / Dagger
# ============================================
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclassmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <methods>;
}
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}
# Lưu ý: @HiltViewModel annotation không cần giữ package name
# ViewModels sẽ được keep với allowobfuscation ở dưới
# HiltViewModel - Chỉ giữ constructor và methods, cho phép obfuscate class name
-keepclassmembers,allowobfuscation class * {
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
# Lưu ý: Class name sẽ bị obfuscate (ChatbotViewModel -> a, b, c...)
# Nhưng Hilt vẫn hoạt động vì giữ constructor và methods
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# ============================================
# Ktor Client
# ============================================
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.serialization.json.** { *; }
-keep class kotlinx.serialization.** { *; }

# ============================================
# Room Database
# ============================================
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn androidx.room.paging.**

# ============================================
# DataStore
# ============================================
-keep class androidx.datastore.** { *; }
-keep class com.baothanhbin.core.datastore.** { *; }
-keepclassmembers class com.baothanhbin.core.datastore.ApiKeyProto$* { *; }
-dontwarn androidx.datastore.**

# ============================================
# Compose
# ============================================
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-dontwarn androidx.compose.**

# ============================================
# Google Generative AI SDK
# ============================================
-keep class com.google.ai.client.generativeai.** { *; }
-keep interface com.google.ai.client.generativeai.** { *; }
-dontwarn com.google.ai.client.generativeai.**

# ============================================
# Android Keystore (Encryption)
# ============================================
# Encryption classes - Cho phép obfuscate package name VÀ class name
# Chỉ giữ methods và fields để app hoạt động
-keepclassmembers,allowobfuscation class com.baothanhbin.core.data.encryption.** {
    <methods>;
    <fields>;
}
# KeyEncryptionManager sẽ bị obfuscate thành: z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5.a

# ============================================
# Reflection (nếu có sử dụng)
# ============================================
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses

# ============================================
# Parcelable
# ============================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# ============================================
# Keep line numbers for crash reports
# ============================================
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Obfuscation Settings (Tăng cường bảo mật)
# ============================================
# Lưu ý: R8 tự động obfuscate khi isMinifyEnabled = true
# Không cần option -obfuscate (chỉ dùng cho ProGuard cũ)

# Tối ưu hóa code
-optimizationpasses 5
-allowaccessmodification

# ============================================
# Package Name Obfuscation - Repackage vào tên lạ, khó đọc
# ============================================
# Repackage tất cả classes vào package ngẫu nhiên, khó đoán
# Sử dụng tên package ngẫu nhiên, không có ý nghĩa để khó dịch ngược
# Lưu ý: R8 chỉ hỗ trợ repackage vào 1 package duy nhất
# Tất cả classes sẽ được repackage vào package này (trừ các class được keep)
-repackageclasses 'z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5'

# Giữ CHỈ các class BẮT BUỘC (App, MainActivity cho AndroidManifest.xml)
# TẤT CẢ class khác sẽ bị obfuscate CẢ package name VÀ class name

# 1. App và MainActivity (BẮT BUỘC - AndroidManifest.xml cần đúng tên)
-keepnames class com.baothanhbin.agridoctorai.App
-keepnames class com.baothanhbin.agridoctorai.MainActivity

# 2. Models (BẮT BUỘC - Serialization cần giữ class names và package)
# Chỉ giữ structure, KHÔNG obfuscate
-keepnames class com.baothanhbin.core.model.**

# 3. DataStore Proto (BẮT BUỘC - Proto classes cần package name)
-keepnames class com.baothanhbin.core.datastore.**

# 4. Hilt/Dagger generated classes (BẮT BUỘC - Hilt cần package name)
-keepnames class com.baothanhbin.**.*_HiltModules
-keepnames class com.baothanhbin.**.*_HiltModules$*
-keepnames class dagger.hilt.**
-keepnames class javax.inject.**


# ============================================
# OBFUSCATE TẤT CẢ - Package names VÀ Class names
# ============================================
# TẤT CẢ các class khác sẽ được:
# 1. Repackage vào 'z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5' ✅
# 2. Class names bị obfuscate thành: a, b, c, d, e... ✅
#
# Bao gồm:
# - feature/** (chatbot, diagnose, camera, etc.) → Package: z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5 ✅
# - ChatbotViewModel, DiagnoseViewModel → Class: a, b, c... ✅
# - core/data/** (repositories, use cases) → Package và Class name obfuscated ✅
# - core/network/** → Package và Class name obfuscated ✅
# - core/database/** (trừ entities và DAOs) → Package và Class name obfuscated ✅
# - core/data/encryption/** → Package và Class name obfuscated ✅
# - Tất cả Screens, Composables, Repositories, UseCases → Đều obfuscated ✅
#
# KẾT QUẢ: Khi decompile APK sẽ thấy:
# - Thay vì: com.baothanhbin.feature.chatbot.ChatbotViewModel
# - Sẽ thấy: z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5.a
# - Methods và fields vẫn giữ để app hoạt động

# Optimizations
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ============================================
# Keep các class cần thiết cho app hoạt động
# ============================================
# Keep Application class (đã được keep ở trên với allowobfuscation)
-keep class com.baothanhbin.agridoctorai.App { *; }

# Keep MainActivity (đã được keep ở trên với allowobfuscation)
-keep class com.baothanhbin.agridoctorai.MainActivity { *; }

# ViewModels - OBFUSCATE TẤT CẢ (package name VÀ class name)
# Hilt có thể hoạt động với obfuscated class names nếu giữ đúng structure
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
    <methods>;
    <fields>;
}
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>(...);
    <methods>;
    <fields>;
}
# Cho phép obfuscate CẢ package name VÀ class name của ViewModels
# Class name sẽ thành: a, b, c, d... thay vì ChatbotViewModel, DiagnoseViewModel

# Keep Navigation (nếu có)
-keepnames class * implements androidx.navigation.NavArgs
-keepclassmembers class * implements androidx.navigation.NavArgs {
    <init>(android.os.Bundle);
}

# ============================================
# Network Models (Keep để serialization hoạt động)
# ============================================
-keep class com.baothanhbin.core.model.** { *; }

# ============================================
# Resources
# ============================================
-keepclassmembers class **.R$* {
    public static <fields>;
}

# ============================================
# Native methods
# ============================================
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================
# Enums
# ============================================
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# ============================================
# Remove logging trong release (tùy chọn)
# ============================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
