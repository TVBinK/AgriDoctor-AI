import com.android.build.api.dsl.ApplicationExtension
import com.baothanhbin.apps.plugins.convention.configAndroidCompose
import com.baothanhbin.apps.plugins.convention.libs
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidApplicationComposePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            apply(plugin = "com.android.application")
            apply(plugin = "org.jetbrains.kotlin.plugin.compose")
//            apply(plugin = "com.google.gms.google-services")
//            apply(plugin = "com.google.firebase.crashlytics")

            val extension = extensions.getByType<ApplicationExtension>()
            configAndroidCompose(extension)

            dependencies {
                add("implementation", project(":resources"))
                add("implementation", project(":core:model"))
                add("implementation", libs.findLibrary("androidx-core-ktx").get())
                add("implementation", libs.findLibrary("androidx-appcompat").get())
                add("implementation", libs.findLibrary("androidx-navigation-compose").get())
            }
        }
    }
}