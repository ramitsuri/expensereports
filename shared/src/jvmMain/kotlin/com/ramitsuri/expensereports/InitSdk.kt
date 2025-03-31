package com.ramitsuri.expensereports

import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.expensereports.database.AppDatabase
import com.ramitsuri.expensereports.di.KoinQualifier
import com.ramitsuri.expensereports.di.initKoin
import com.ramitsuri.expensereports.shared.BuildKonfig
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import okio.Path
import okio.Path.Companion.toPath
import org.koin.dsl.module
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.absolutePathString

fun initSdk() {
    initKoin {
        module {
            factory<HttpClientEngine> {
                Java.create()
            }

            factory<RoomDatabase.Builder<AppDatabase>> {
                Files.createDirectories(appDir)
                val fileName = get<String>(qualifier = KoinQualifier.DATABASE_NAME)
                val dbFile = appDir.resolve(fileName).toFile()
                Room.databaseBuilder<AppDatabase>(
                    name = dbFile.absolutePath,
                )
            }

            factory<Path> {
                val fileName = get<String>(qualifier = KoinQualifier.DATASTORE_FILE_NAME)
                appDir.resolve(fileName).absolutePathString().toPath()
            }

            factory<Boolean>(qualifier = KoinQualifier.IS_DESKTOP) {
                true
            }
        }
    }
}

// TODO make compatible with other desktop OSs
private val appDir =
    System.getProperty("user.home")
        .let { userHomePath ->
            val osPath =
                System
                    .getProperty("os.name", "generic")
                    .lowercase()
                    .let { os ->
                        if ((os.indexOf("mac") >= 0) || (os.indexOf("darwin") >= 0)) {
                            "Library"
                        } else if (os.indexOf("win") >= 0) {
                            "Documents"
                        } else {
                            error("OS not supported")
                        }
                    }
            val appPath = "com.ramitsuri.expensereports"
            val buildPath = if (BuildKonfig.IS_DEBUG) "debug" else "release"
            Paths.get(userHomePath, osPath, appPath, buildPath)
        }
