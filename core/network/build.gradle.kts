plugins {
    alias(libs.plugins.agridoctor.compose.module)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.baothanhbin.core.network"

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        val apiBaseUrl = (findProperty("api.baseUrl") as String?)
            ?: System.getenv("API_BASE_URL")
            ?: "https://api.example.com"
        buildConfigField("String", "API_BASE_URL", "\"${apiBaseUrl.trimEnd('/')}\"")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(projects.core.model)

    // Ktor client
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation(libs.ktor.serialization.kotlinx.json)

}
