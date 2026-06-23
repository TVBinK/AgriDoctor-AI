plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnosefailed"
}

dependencies {
    //core
    implementation(projects.core.theme)
    implementation(projects.core.ui)
    implementation(projects.resources)
    implementation(projects.core.database)
    implementation(projects.core.model)
    //coil
    implementation(libs.coil.compose)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
}
