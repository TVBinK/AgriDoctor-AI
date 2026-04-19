import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.agridoctor.compose.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.agridoctor.hilt)
}

// Load keystore properties
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    namespace = "com.baothanhbin.agridoctorai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.baothanhbin.agridoctorai"
        minSdk = 27
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            // Đọc thông tin từ keystore.properties
            if (keystoreProperties.isNotEmpty()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            // Bật code obfuscation và minification
            isMinifyEnabled = true
            isShrinkResources = true // Xóa resources không sử dụng
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Sử dụng signing config
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.biometric)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    //Modules
    implementation(projects.core.model)
    implementation(projects.core.data)
    implementation(projects.resources)
    implementation(projects.feature.home)
    implementation(projects.feature.chatbot)
    implementation(projects.feature.myplants)
    implementation(projects.feature.diagnose)
    implementation(projects.feature.camera)
    implementation(projects.feature.processimage)
    implementation(projects.feature.diagnoseresult)
    implementation(projects.feature.diagnosefailed)
    implementation(projects.feature.lightmeter)
    implementation(projects.feature.settings)
    implementation(projects.feature.login)
    implementation(projects.feature.forgotpassword)
    implementation(projects.feature.signup)
    implementation(projects.feature.verificationotp)
    implementation(projects.core.ui)
    implementation(projects.core.theme)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
}
