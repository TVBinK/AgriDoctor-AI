plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.diagnose"
}

dependencies {
    //core
    implementation(project(":core:theme"))
}