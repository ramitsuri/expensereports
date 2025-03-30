package com.ramitsuri.expensereports.testutils

import androidx.room.Room
import androidx.room.RoomDatabase
import com.ramitsuri.expensereports.database.AppDatabase
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java
import okio.Path
import okio.Path.Companion.toOkioPath
import org.koin.dsl.module
import java.nio.file.Paths
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
val testModule =
    module {
        factory<HttpClientEngine> {
            HttpClient(Java)
            Java.create()
        }

        factory<RoomDatabase.Builder<AppDatabase>> {
            Room.inMemoryDatabaseBuilder<AppDatabase>()
        }

        factory<Path> {
            Paths.get(BaseTest.TEMP_DIR).resolve("${Uuid.random()}.preferences_pb").toOkioPath()
        }
    }
