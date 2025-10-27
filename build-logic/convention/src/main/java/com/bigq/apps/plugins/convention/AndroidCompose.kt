package com.bigq.apps.plugins.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

fun Project.configAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>
) {
    commonExtension.apply {
        buildFeatures {
            compose = true
            buildConfig = true
        }

        dependencies {
            add(
                "implementation", platform(
                    libs.findLibrary("androidx-compose-bom").get()
                )
            )
            add("implementation", libs.findLibrary("androidx-material3").get())
            add("implementation", libs.findLibrary("androidx-ui-tooling-preview").get())
            add("implementation", libs.findLibrary("compose-material-icons-extended").get())
            add("debugImplementation", libs.findLibrary("androidx-ui-tooling").get())
        }
    }
}
