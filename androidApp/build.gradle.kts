plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.ramitsuri.expensereports.android"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.ramitsuri.expensereports.android"
        minSdk = 28
        targetSdk = 34
        versionCode = 26
        versionName = "2.6"
    }
    packagingOptions {
        resources.excludes.add("META-INF/*.kotlin_module")
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    buildFeatures {
        compose = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xjvm-default=all-compatibility"
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":shared"))
    implementation(libs.bundles.app.ui)
    implementation(libs.multiplatformSettings.common)
    implementation(libs.kotlinx.dateTime)
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
    implementation(libs.kotlin.bignum)
    implementation(libs.android.work)
    testImplementation(libs.junit)
}