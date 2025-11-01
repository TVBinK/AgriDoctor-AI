plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnosefailed"
}

dependencies {
    //core
    implementation(project(":core:theme"))
    implementation(project(":resources"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    //coil
    implementation(libs.coil.compose)
}