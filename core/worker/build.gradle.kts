plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.agridoctor.hilt)
}

android {
    namespace = "com.baothanhbin.core.worker"
}

dependencies {
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.resources)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    implementation(libs.hilt.android)
    implementation(libs.androidx.core.ktx)
}
