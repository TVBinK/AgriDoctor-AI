plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.chatbot"
    
    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(projects.core.theme)
    implementation(projects.core.ui)
    implementation(projects.core.network)
    implementation(projects.core.database)
    implementation(projects.core.data)
    implementation(projects.resources)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.coil.compose)
}
