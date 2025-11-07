plugins {
    alias(libs.plugins.agridoctor.compose.module)
}

android {
    namespace = "com.baothanhbin.core.ui"
}

dependencies {
    implementation(project(":app"))
}