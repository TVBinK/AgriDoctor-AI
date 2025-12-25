plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.signup"

}

dependencies {
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(project(":core:theme"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:ui"))
}