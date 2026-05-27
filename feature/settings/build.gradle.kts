plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.settings"
}

dependencies {
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.resources)
    implementation(projects.core.ui)
    implementation(projects.resources)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.biometric)
    implementation(libs.androidx.compose.foundation)
}

