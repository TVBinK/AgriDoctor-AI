plugins {
    alias(libs.plugins.agridoctor.compose.module)
}

android {
    namespace = "com.baothanhbin.core.ui"
}

dependencies {
    implementation(projects.resources)
    implementation(projects.core.theme)
    implementation(libs.androidx.navigation.compose)
    
    // Google Play Services Location for Fused Location Provider
    implementation(libs.play.services.location)
}
