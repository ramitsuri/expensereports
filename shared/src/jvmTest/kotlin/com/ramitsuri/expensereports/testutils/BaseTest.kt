package com.ramitsuri.expensereports.testutils

import com.ramitsuri.expensereports.di.coreModule
import org.junit.After
import org.junit.Rule
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

open class BaseTest : KoinTest {
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        printLogger()
        modules(testModule, coreModule)
    }

    @OptIn(ExperimentalPathApi::class)
    @After
    fun tearDown() {
        stopKoin()
        Paths.get(TEMP_DIR).deleteRecursively()
    }

    companion object {
        const val TEMP_DIR = "temp"
    }
}
