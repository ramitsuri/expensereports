import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.BOOLEAN
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type.STRING
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.buildKonfig)
    alias(libs.plugins.ktlint)
}

kotlin {
    jvm()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.navigation.compose)
            implementation(libs.androidx.lifecycle.runtime)
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
            implementation(libs.androidx.lifecycle.viewmodel.savedstate)
            implementation(libs.lifecycle.runtime.compose)

            implementation(libs.room.ktx)
            implementation(libs.room.runtime)
            implementation(libs.androidx.sqlite.bundled)
            implementation(libs.koin.core)
            implementation(libs.kotlin.datetime)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)

            implementation(libs.kotlin.serialization)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.content.negotation)
            implementation(libs.ktor.logging)
            implementation(libs.ktor.serialization)

            implementation(libs.datastore)

            implementation(libs.kermit)
        }
        androidMain.dependencies {
            implementation(libs.androidx.ktx)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.playservices.coroutines)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.koin.android)
            implementation(libs.koin.core)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose.viewmodel.navigation)
            implementation(libs.koin.workmanager)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.ktor.client.android)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutines.swing)
            implementation(libs.jsystemthemedetector)
            implementation(libs.ktor.client.java)
        }
        jvmTest.dependencies {
            implementation(libs.androidx.room.testing)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.koin.test)
            implementation(libs.koin.test.junit)
            implementation(libs.turbine)
        }
    }

    // To fix `java.lang.IllegalStateException: Module with the Main dispatcher had failed to
    // initialize.` For some reason android artifact was being pulled, found this as the only way to
    // fix it.
    configurations.commonMainApi {
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
    }
}

android {
    namespace = "com.ramitsuri.expensereports"
    compileSdk = 35

    defaultConfig {
        minSdk = 30
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    add("kspAndroid", libs.room.compiler)
    add("kspJvm", libs.room.compiler)
    add("kspJvmTest", libs.room.compiler)
}

room {
    schemaDirectory("$projectDir/schemas")
}

compose.resources {
    publicResClass = true
}

compose.desktop {
    application {
        mainClass = "com.ramitsuri.expensereports.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Expense Reports"
            packageVersion = "1.0.0"

            val iconsRoot = project.file("desktop-icons")
            macOS {
                iconFile.set(iconsRoot.resolve("macos.icns"))
            }
            // https://issuetracker.google.com/issues/342609814
            modules("jdk.unsupported")
        }
    }
}

buildkonfig {
    packageName = "com.ramitsuri.expensereports.shared"

    val isDebug = getLocalProperty("isDebug", "false") ?: "false"
    val appVersion = libs.versions.appVersion.get()

    defaultConfigs {
        buildConfigField(type = BOOLEAN, name = "IS_DEBUG", value = isDebug, const = true)
        buildConfigField(type = STRING, name = "APP_VERSION", value = appVersion, const = true)
    }
}

fun getLocalProperty(
    key: String,
    defaultValue: String? = null,
): String? {
    val file = "local.properties"
    return getProperty(file, key, defaultValue)
}

fun getProperty(
    fileName: String,
    key: String,
    defaultValue: String? = null,
): String? {
    return if (file(rootProject.file(fileName)).exists()) {
        val properties = Properties()
        properties.load(FileInputStream(file(rootProject.file(fileName))))
        properties.getProperty(key, defaultValue)
    } else {
        defaultValue
    }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude { element -> element.file.toString().contains("generated/") }
        exclude { element -> element.file.toString().contains("build/") }
    }
}
