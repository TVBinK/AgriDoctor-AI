plugins {
    alias(libs.plugins.agridoctor.compose.module)
}

android {
    namespace = "com.baothanhbin.core.network"
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)

    // Ktor client
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation(libs.ktor.serialization.kotlinx.json)

}