plugins {
    alias(libs.plugins.agridoctor.feature)
}

android {
    namespace = "com.baothanhbin.feature.home"
}

dependencies {
    //coil
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    //core
    implementation(projects.core.theme)
    implementation(projects.core.database)
    implementation(projects.feature.chatbot)
    implementation(projects.feature.camera)
    implementation(projects.feature.lightmeter)
    implementation(projects.feature.settings)
    implementation(projects.feature.myplants)
    implementation(projects.core.ui)
    implementation(projects.core.data)
    implementation(projects.core.alarm)
}
