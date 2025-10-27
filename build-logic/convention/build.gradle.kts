import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly("androidx.room:room-gradle-plugin:2.8.2")
}

gradlePlugin {
    plugins {
        register("androidComposeAppication") {
            id = "agridoctor.compose.application"
            implementationClass = "AndroidApplicationComposePlugin"
        }
        register("androidComposeLibrary") {
            id = "agridoctor.compose.module"
            implementationClass = "AndroidLibraryComposePlugin"
        }
        register("featureConvention") {
            id = "agridoctor.feature"
            implementationClass = "AndroidFeaturePlugin"
        }
        register("roomConvention") {
            id = "agridoctor.room"
            implementationClass = "AndroidRoomPlugin"
        }
        register("hiltConvention") {
            id = "agridoctor.hilt"
            implementationClass = "HiltConventionPlugin"
        }
        register("androidModuleConvention") {
            id = "agridoctor.module"
            implementationClass = "AndroidModuleConvention"
        }
    }
}
