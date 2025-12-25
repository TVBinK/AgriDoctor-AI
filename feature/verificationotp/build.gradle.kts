plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.verificationotp"
}

dependencies {
    implementation(project(":core:theme"))
    implementation(project(":resources"))
    implementation(project(":core:ui"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    
    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
}