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
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.model)
    implementation(projects.core.ui)
}