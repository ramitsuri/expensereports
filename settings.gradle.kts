pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "ExpenseReports"
include(":androidApp")
include(":shared")

enableFeaturePreview("VERSION_CATALOGS")