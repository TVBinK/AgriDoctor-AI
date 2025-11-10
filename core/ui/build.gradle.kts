plugins {
    alias(libs.plugins.agridoctor.compose.module)
}

android {
    namespace = "com.baothanhbin.core.ui"
}

dependencies {
    implementation(project(":resources"))
    implementation(project(":core:theme"))
}