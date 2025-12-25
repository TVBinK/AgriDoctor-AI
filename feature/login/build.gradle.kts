plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.login"
}

dependencies {
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    //core
    implementation(project(":core:theme"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
}