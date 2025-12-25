package com.baothanhbin.agridoctorai.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.security.MessageDigest

/**
 * SecurityManager - Kiểm tra tính toàn vẹn và bảo mật của ứng dụng
 * Phát hiện các hành vi tampering, rebuild/recompile không hợp pháp
 */
object SecurityManager {

    private const val EXPECTED_SIGNATURE_SHA256 = "f3036ab303b09bacae8d2c261ab106fffb1bfe1c62ac19748247588ba59b1d393"
    private const val PLACEHOLDER_SIGNATURE = "YOUR_RELEASE_SIGNATURE_SHA256_HERE"
    private const val ALGORITHM_SHA256 = "SHA-256"

    /**
     * Thực hiện kiểm tra bảo mật toàn diện
     */
    fun performSecurityCheck(context: Context): SecurityCheckResult {
        val issues = mutableListOf<SecurityIssue>()

        if (!isSignatureValid(context)) {
            issues.add(
                SecurityIssue(
                    type = SecurityIssueType.INVALID_SIGNATURE,
                    message = "Chữ ký ứng dụng không hợp lệ. App có thể đã bị chỉnh sửa."
                )
            )
        }

        return SecurityCheckResult(
            isSecure = issues.isEmpty(),
            issues = issues
        )
    }

    /**
     * Kiểm tra tính hợp lệ của chữ ký ứng dụng
     * @return true nếu chữ ký hợp lệ hoặc đang ở debug mode
     */
    private fun isSignatureValid(context: Context): Boolean {
        return try {
            if (isDebuggable(context)) return true

            val signatures = getAppSignatures(context) ?: return false
            if (signatures.isEmpty()) return false

            val currentSignature = getSignatureSha256(signatures[0])
            currentSignature == EXPECTED_SIGNATURE_SHA256 || 
            EXPECTED_SIGNATURE_SHA256 == PLACEHOLDER_SIGNATURE
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Lấy danh sách chữ ký của ứng dụng
     */
    private fun getAppSignatures(context: Context): Array<out Signature>? {
        return try {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.signingInfo?.apkContentsSigners
            } else {
                @Suppress("DEPRECATION")
                packageInfo.signatures
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Tính SHA-256 hash của chữ ký
     */
    private fun getSignatureSha256(signature: Signature): String {
        val digest = MessageDigest.getInstance(ALGORITHM_SHA256)
        val hash = digest.digest(signature.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Kiểm tra ứng dụng có đang ở chế độ debug không
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
        if (isSecure) return "Ứng dụng an toàn."

        val criticalIssues = issues.filter { 
            it.type == SecurityIssueType.INVALID_SIGNATURE
        }

        return if (criticalIssues.isNotEmpty()) {
            buildString {
                append("⚠️ CẢNH BÁO BẢO MẬT\n\n")
                append("Ứng dụng có thể đã bị chỉnh sửa bất hợp pháp:\n\n")
                append(criticalIssues.joinToString("\n") { "• ${it.message}" })
                append("\n\nVui lòng tải ứng dụng chính thức.")
            }
        } else {
            "⚠️ Cảnh báo\n\n${issues.joinToString("\n") { "• ${it.message}" }}"
        }
    }

    /**
     * Kiểm tra có vấn đề nghiêm trọng không (cần chặn app)
     */
    fun hasCriticalIssues(): Boolean {
        return issues.any { it.type == SecurityIssueType.INVALID_SIGNATURE }
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
    INVALID_SIGNATURE    // Chữ ký không hợp lệ
}

