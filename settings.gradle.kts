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

rootProject.name = "AgriDoctor AI"
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
