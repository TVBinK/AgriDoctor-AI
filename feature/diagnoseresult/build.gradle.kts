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
}