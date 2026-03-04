import com.bigq.apps.plugins.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeaturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply {
                apply("agridoctor.compose.module")
                apply("agridoctor.hilt")
                apply("org.jetbrains.kotlin.plugin.serialization")
            }

            dependencies {
                add("implementation", project(":resources"))
                add("implementation", project(":core:model"))
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            }
        }
    }
}