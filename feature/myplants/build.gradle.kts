plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.myplants"

}

dependencies {
    implementation(project(":resources"))
    implementation(project(":core:model"))
    implementation(project(":core:theme"))
    implementation(project(":core:ui"))
}