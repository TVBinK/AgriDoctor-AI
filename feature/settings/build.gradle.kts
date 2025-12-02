plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.settings"
}

dependencies {
    implementation(project(":core:theme"))
    implementation(project(":core:data"))
    implementation(project(":resources"))
    implementation(project(":core:ui"))
    implementation(project(":resources"))
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.compose.foundation)
}

