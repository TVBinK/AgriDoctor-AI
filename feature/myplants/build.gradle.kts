plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.myplants"

}

dependencies {
    implementation(projects.resources)
    implementation(projects.core.model)
    implementation(projects.core.theme)
    implementation(projects.core.ui)
}