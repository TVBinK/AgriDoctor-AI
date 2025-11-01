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
    implementation(project(":core:theme"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":resources"))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.generativeai)
}