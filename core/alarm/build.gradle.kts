plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.agridoctor.hilt)
}

android {
    namespace = "com.baothanhbin.core.alarm"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.model)
    implementation(projects.resources)
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.ktx)
}
