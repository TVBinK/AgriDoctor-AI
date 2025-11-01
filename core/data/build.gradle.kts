plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.agridoctor.hilt)
}

android {
    namespace = "com.baothanhbin.core.data"
}

dependencies {
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(libs.hilt.android)
}