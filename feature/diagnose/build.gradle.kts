plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnose"
}

dependencies {
    //core
    implementation(project(":core:theme"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":feature:diagnoseresult"))
    implementation(project(":feature:camera"))
    implementation(project(":core:ui"))

    //coil
    implementation(libs.coil.compose)
}