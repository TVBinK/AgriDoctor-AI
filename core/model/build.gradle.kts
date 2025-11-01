plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.baothanhbin.core.model"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.kotlinx.serialization.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}