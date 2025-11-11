plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.agridoctor.room)
    alias(libs.plugins.agridoctor.hilt)
    alias(libs.plugins.jetbrains.kotlin.plugin.serialization)
}

android {
    namespace = "com.baothanhbin.core.database"
}

dependencies {
    implementation(project(":core:model"))
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    implementation(libs.android.database.sqlcipher)
    implementation(libs.androidx.sqlite)
    implementation(libs.androidx.sqlite.framework)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
}