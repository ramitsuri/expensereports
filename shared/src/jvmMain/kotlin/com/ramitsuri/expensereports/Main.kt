package com.ramitsuri.expensereports

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.lifecycle.compose.LifecycleResumeEffect
import com.ramitsuri.expensereports.ui.navigation.NavGraph
import com.ramitsuri.expensereports.ui.theme.AppTheme
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.app_name
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import org.jetbrains.compose.resources.stringResource

fun main() =
    application {
        initSdk()
        var sizeIncreasedLastTime by remember { mutableStateOf(false) }

        val windowState =
            rememberWindowState(
                size = DpSize(800.dp, 600.dp),
                position = WindowPosition.PlatformDefault,
            )

        Window(
            onCloseRequest = ::exitApplication,
            title = stringResource(Res.string.app_name),
            state = windowState,
        ) {
            AppTheme {
                NavGraph()
            }
            LaunchedEffect(windowState) {
                snapshotFlow { windowState.size }
                    .launchIn(this)

                snapshotFlow { windowState.position }
                    .filter { it.isSpecified }
                    .launchIn(this)
            }
            LifecycleResumeEffect(Unit) {
                // Due to a bug where dialogs don't open in the center of the window but resizing the
                // window fixes it
                if (sizeIncreasedLastTime) {
                    windowState.size -= DpSize(1.dp, 1.dp)
                    sizeIncreasedLastTime = false
                } else {
                    windowState.size += DpSize(1.dp, 1.dp)
                    sizeIncreasedLastTime = true
                }
                onPauseOrDispose {}
            }
        }
    }
