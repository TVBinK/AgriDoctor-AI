plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.processimage"
}

dependencies {
    implementation(project(":resources"))
    implementation(project(":core:theme"))
    implementation(project(":core:network"))
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
}