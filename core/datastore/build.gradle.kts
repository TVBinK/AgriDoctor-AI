plugins {
    alias(libs.plugins.agridoctor.module)
    alias(libs.plugins.agridoctor.hilt)
    alias(libs.plugins.google.protobuf)
}

android {
    namespace = "com.baothanhbin.core.datastore"
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
    implementation(project(":core:database"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(libs.hilt.android)
    implementation(libs.androidx.datastore)
    implementation(libs.protobuf.kotlin.lite)
}