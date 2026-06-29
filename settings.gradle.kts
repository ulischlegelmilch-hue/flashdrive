pluginManagement {
    repositories {
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://dl.google.com/dl/android/maven2/") }
        mavenCentral()
    }
}

rootProject.name = "FlashDrive"
include(":app")
include(":core:model")
include(":core:database")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:UI")
include(":feature:study")
include(":feature:statistics")
include(":feature:import")
include(":feature:settings")
//include(":feature:auto") // temporarily disabled - Android Auto module
include(":feature:deck")
