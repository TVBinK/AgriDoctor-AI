plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnose"
}

dependencies {
    //core
    implementation(projects.core.theme)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.feature.diagnoseresult)
    implementation(projects.feature.camera)
    implementation(projects.feature.settings)
    implementation(projects.core.ui)

    //coil
    implementation(libs.coil.compose)
}
