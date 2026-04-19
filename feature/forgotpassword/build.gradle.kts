plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.forgotpassword"
}

dependencies {
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    implementation(projects.resources)
    implementation(libs.androidx.compose.foundation)
}
