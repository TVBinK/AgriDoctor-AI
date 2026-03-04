plugins {
    alias(libs.plugins.agridoctor.compose.module)
}

android {
    namespace = "com.baothanhbin.core.theme"
}

dependencies {
    implementation(projects.resources)
}