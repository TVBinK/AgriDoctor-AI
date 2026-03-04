import com.bigq.apps.plugins.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class HiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
            }

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("implementation", libs.findLibrary("androidx.hilt.navigation.compose").get())
                add("ksp", libs.findLibrary("dagger-hilt-complier").get())
                add("ksp", libs.findLibrary("androidx-hilt-complier").get())
            }
        }
    }
}