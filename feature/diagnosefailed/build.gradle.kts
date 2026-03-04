plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnosefailed"
}

dependencies {
    //core
    implementation(projects.core.theme)
    implementation(projects.resources)
    implementation(projects.core.database)
    implementation(projects.core.model)
    //coil
    implementation(libs.coil.compose)
}