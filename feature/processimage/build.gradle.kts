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
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":feature:diagnoseresult"))
    implementation(project(":feature:diagnosefailed"))
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
}