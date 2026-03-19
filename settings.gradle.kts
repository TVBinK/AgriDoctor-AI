pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "AgriDoctorAI"
includeBuild("build-logic")
include(":app")
include(":core:model")
include(":feature:home")
include(":resources")
include(":core:theme")
include(":feature:diagnose")
include(":feature:myplants")
include(":feature:chatbot")
include(":feature:camera")
include(":feature:processimage")
include(":feature:diagnoseresult")
include(":core:network")
include(":core:data")
include(":core:database")
include(":feature:diagnosefailed")
include(":core:datastore")
include(":core:ui")
include(":feature:lightmeter")
include(":feature:settings")
include(":feature:login")
include(":feature:signup")
include(":feature:verificationotp")
include(":core:worker")
