plugins {
    alias(libs.plugins.agridoctor.compose.module)
}

android {
    namespace = "com.baothanhbin.core.ui"
}

dependencies {
    implementation(project(":resources"))
    implementation(project(":core:theme"))
    
    // Google Play Services Location for Fused Location Provider
    implementation(libs.play.services.location)
}