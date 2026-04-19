plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.processimage"
}

dependencies {
    implementation(projects.resources)
    implementation(projects.core.theme)
    implementation(projects.core.network)
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.core.database)
    implementation(projects.core.ui)
    implementation(projects.core.model)
    implementation(projects.feature.diagnoseresult)
    implementation(projects.feature.diagnosefailed)
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.androidx.exifinterface)
}
