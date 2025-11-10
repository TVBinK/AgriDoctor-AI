plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.home"
}

dependencies {
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    //core
    implementation(project(":core:theme"))
    implementation(project(":feature:chatbot"))
    implementation(project(":feature:camera"))
    implementation(project(":feature:lightmeter"))
    implementation(project(":core:ui"))
}