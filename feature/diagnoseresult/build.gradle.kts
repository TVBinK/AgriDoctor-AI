plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnoseresult"
}

dependencies {
    //core
    implementation(projects.core.theme)
    implementation(projects.resources)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.model)
    implementation(projects.core.ui)
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    //kotlinx serialization
    implementation(libs.kotlinx.serialization.json)
}