plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnoseresult"
}

dependencies {
    //core
    implementation(project(":core:theme"))
    implementation(project(":resources"))
    implementation(project(":core:data"))
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    //coil
    implementation(libs.coil.compose)
}