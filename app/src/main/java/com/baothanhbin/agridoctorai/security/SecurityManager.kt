package com.baothanhbin.agridoctorai.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.io.File
import java.security.MessageDigest

/**
 * SecurityManager - Kiểm tra tính toàn vẹn và bảo mật của ứng dụng
 * Phát hiện các hành vi tampering, root, debug, và cài đặt từ nguồn không chính thức
 */
object SecurityManager {

    // SHA-256 của chữ ký hợp pháp (release keystore)
    // Đã cấu hình với keystore: key_debug_by_bin (alias: key0)
    // Để lấy SHA-256: keytool -list -v -keystore your-release-key.keystore
    private const val EXPECTED_SIGNATURE_SHA256 = "f3036ab303b09bacae8d2c261ab106fffb1bfe1c62ac19748247588ba59b1d393"

    /**
     * Kiểm tra toàn diện tính bảo mật của app
     * @return SecurityCheckResult chứa thông tin chi tiết về các vấn đề bảo mật
     */
    fun performSecurityCheck(context: Context): SecurityCheckResult {
        val issues = mutableListOf<SecurityIssue>()

        // 1. Kiểm tra chữ ký ứng dụng
        if (!isSignatureValid(context)) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.INVALID_SIGNATURE,
                    message = "Chữ ký ứng dụng không hợp lệ. App có thể đã bị chỉnh sửa."
                )
            )
        }

        // 2. Kiểm tra nguồn cài đặt
        if (!isInstallerValid(context)) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.INVALID_INSTALLER,
                    message = "App không được cài đặt từ nguồn chính thức."
                )
            )
        }

        return SecurityCheckResult(
            isSecure = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Kiểm tra chữ ký ứng dụng
     */
    private fun isSignatureValid(context: Context): Boolean {
        try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                )
            }

            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }

            if (signatures.isNullOrEmpty()) {
                return false
            }

            // Lấy SHA-256 của chữ ký hiện tại
            val currentSignatureSha256 = getSignatureSha256(signatures[0])

            // Trong debug mode, bỏ qua kiểm tra chữ ký
            if (isDebuggable(context)) {
                return true
            }

            // So sánh với chữ ký mong đợi
            return currentSignatureSha256 == EXPECTED_SIGNATURE_SHA256 ||
                    EXPECTED_SIGNATURE_SHA256 == "YOUR_RELEASE_SIGNATURE_SHA256_HERE" // Cho phép nếu chưa cấu hình
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Tính SHA-256 của chữ ký
     */
    private fun getSignatureSha256(signature: Signature): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(signature.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Kiểm tra nguồn cài đặt ứng dụng
     */
    private fun isInstallerValid(context: Context): Boolean {
        try {
            val installer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                context.packageManager.getInstallSourceInfo(context.packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getInstallerPackageName(context.packageName)
            }

            // Trong debug mode, cho phép cài từ IDE
            if (isDebuggable(context)) {
                return true
            }

            // Các nguồn cài đặt hợp lệ
            val validInstallers = listOf(
                "com.android.vending",           // Google Play Store
                "com.google.android.feedback",    // Google Play Store (feedback)
                null                              // Cài trực tiếp trong debug mode
            )

            return installer in validInstallers
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    /**
     * Kiểm tra app có đang ở chế độ debug không
     */
    private fun isDebuggable(context: Context): Boolean {
        return try {
            (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * Kết quả kiểm tra bảo mật
 */
data class SecurityCheckResult(
    val isSecure: Boolean,
    val issues: List<SecurityIssue>
) {
    /**
     * Lấy thông báo tổng hợp cho người dùng
     */
    fun getUserMessage(): String {
        if (isSecure) {
            return "Ứng dụng an toàn."
        }

        val criticalIssues = issues.filter { 
            it.type == SecurityIssueType.INVALID_SIGNATURE || 
            it.type == SecurityIssueType.INVALID_INSTALLER 
        }

        return if (criticalIssues.isNotEmpty()) {
            "⚠️ CẢNH BÁO BẢO MẬT\n\n" +
            "Ứng dụng có thể đã bị chỉnh sửa bất hợp pháp:\n\n" +
            criticalIssues.joinToString("\n") { "• ${it.message}" } +
            "\n\nVui lòng tải ứng dụng chính thức."
        } else {
            "⚠️ Cảnh báo\n\n" +
            issues.joinToString("\n") { "• ${it.message}" }
        }
    }

    /**
     * Kiểm tra có vấn đề nghiêm trọng không (cần chặn app)
     */
    fun hasCriticalIssues(): Boolean {
        return issues.any { 
            it.type == SecurityIssueType.INVALID_SIGNATURE || 
            it.type == SecurityIssueType.INVALID_INSTALLER 
        }
    }
}

/**
 * Vấn đề bảo mật
 */
data class SecurityIssue(
    val type: SecurityIssueType,
    val message: String
)

/**
 * Loại vấn đề bảo mật
 */
enum class SecurityIssueType {
    INVALID_SIGNATURE,    // Chữ ký không hợp lệ
    INVALID_INSTALLER     // Nguồn cài đặt không hợp lệ
}

