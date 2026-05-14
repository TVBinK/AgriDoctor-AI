# ============================================
# Kotlin & Coroutines
# ============================================
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Metadata { public <methods>; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ============================================
# Kotlin Serialization
# ============================================
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.baothanhbin.core.model.**$$serializer { *; }
-keepclassmembers class com.baothanhbin.core.model.** { *** Companion; }
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
    @javax.inject.Inject <init>(...);
    @dagger.hilt.android.lifecycle.HiltViewModel <init>(...);
}
-keepnames class com.baothanhbin.**.*_HiltModules
-keepnames class com.baothanhbin.**.*_HiltModules$*
-keepnames class dagger.hilt.**
-keepnames class javax.inject.**
-dontwarn dagger.hilt.**
-dontwarn javax.inject.**

# ============================================
# Ktor & Libraries
# ============================================
-keep class io.ktor.** { *; }
-keep interface io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class kotlinx.serialization.** { *; }
-keep class androidx.datastore.** { *; }
-keep class androidx.compose.** { *; }
-keep interface androidx.compose.** { *; }
-dontwarn androidx.compose.**
# ============================================
# Room Database & DataStore
# ============================================
-keepclassmembers class * { @androidx.room.* <methods>; }
-dontwarn androidx.room.paging.**
-keep class com.baothanhbin.core.datastore.** { *; }
-keepclassmembers class com.baothanhbin.core.datastore.ApiKeyProto$* { *; }
-dontwarn androidx.datastore.**

# ============================================
# Attributes & Annotations
# ============================================
-keepattributes Signature,*Annotation*,EnclosingMethod,InnerClasses
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# Required Classes (Keep names for manifest/serialization)
# ============================================
-keep class com.baothanhbin.agridoctorai.App { *; }
-keep class com.baothanhbin.agridoctorai.MainActivity { *; }
-keepnames class com.baothanhbin.agridoctorai.App
-keepnames class com.baothanhbin.agridoctorai.MainActivity
-keepnames class com.baothanhbin.core.model.**
-keepnames class com.baothanhbin.core.datastore.**

# ============================================
# ViewModels (Allow obfuscation)
# ============================================
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...); <methods>; <fields>;
}
-keepclassmembers,allowobfuscation class * extends androidx.lifecycle.ViewModel {
    <init>(...); <methods>; <fields>;
}

# ============================================
# Security & Encryption (Obfuscate everything)
# ============================================
-keepclassmembers,allowobfuscation class com.baothanhbin.core.data.encryption.** {
    <methods>; <fields>;
}

# ============================================
# Package Obfuscation (Repackage all classes)
# ============================================
-repackageclasses 'z9x8w7v6u5t4s3r2q1p0o9n8m7l6k5j4i3h2g1f0e9d8c7b6a5'

# ============================================
# Optimization & Minification
# ============================================
-optimizationpasses 5
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# ============================================
# Common Android Rules
# ============================================
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclasseswithmembernames class * { native <methods>; }
-keepclassmembers class **.R$* { public static <fields>; }
-keepnames class * implements androidx.navigation.NavArgs
-keepclassmembers class * implements androidx.navigation.NavArgs {
    <init>(android.os.Bundle);
}

# ============================================
# Remove Logging in Release
# ============================================
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
}
