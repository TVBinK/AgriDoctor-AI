plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.agridoctor.hilt)
    alias(libs.plugins.google.protobuf)
}

android {
    namespace = "com.baothanhbin.core.data"
}

protobuf {
    protoc {
        artifact = libs.protobuf.protoc.get().toString()
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                val java by registering {
                    option("lite")
                }
                val kotlin by registering {
                    option("lite")
                }
            }
        }
    }
}

dependencies {
    implementation(projects.core.database)
    implementation(projects.core.datastore)
    implementation(projects.core.model)
    implementation(projects.core.network)
    implementation(libs.hilt.android)
    implementation(libs.protobuf.kotlin.lite)
    // Ktor client
    implementation("io.ktor:ktor-client-core:2.3.12")
    implementation("io.ktor:ktor-client-android:2.3.12")
    implementation("io.ktor:ktor-client-okhttp:2.3.12")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.12")
    implementation(libs.kotlinx.serialization.json)
}