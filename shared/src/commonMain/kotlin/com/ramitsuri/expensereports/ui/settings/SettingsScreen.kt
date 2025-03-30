package com.ramitsuri.expensereports.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramitsuri.expensereports.utils.friendlyDate
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.cancel
import expensereports.shared.generated.resources.ok
import expensereports.shared.generated.resources.settings_fetch_never
import expensereports.shared.generated.resources.settings_last_fetch_time
import expensereports.shared.generated.resources.settings_last_full_fetch_time
import expensereports.shared.generated.resources.settings_url
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewState: SettingsViewState,
    onUrlSet: (String) -> Unit,
    onBack: () -> Unit,
) {
    var showUrlDialog by remember { mutableStateOf(false) }
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding(),
    ) {
        val scrollBehavior =
            TopAppBarDefaults.enterAlwaysScrollBehavior(
                rememberTopAppBarState(),
            )
        Toolbar(
            scrollBehavior = scrollBehavior,
            onBack = onBack,
        )
        LazyColumn(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .padding(16.dp),
        ) {
            item {
                SettingsItem(
                    title = stringResource(Res.string.settings_url),
                    subtitle = viewState.url,
                    onClick = { showUrlDialog = true },
                    showProgress = false,
                )
                SettingsItem(
                    title = stringResource(Res.string.settings_last_fetch_time),
                    subtitle = viewState.lastFetchTime.friendlyFetchDate(),
                    onClick = { },
                    showProgress = false,
                )
                SettingsItem(
                    title = stringResource(Res.string.settings_last_full_fetch_time),
                    subtitle = viewState.lastFullFetchTime.friendlyFetchDate(),
                    onClick = { },
                    showProgress = false,
                )
            }
        }
    }
    if (showUrlDialog) {
        BaseUrlDialog(
            show = showUrlDialog,
            url = "",
            onUrlSet = {
                showUrlDialog = false
                onUrlSet(it)
            },
            onDismiss = {
                showUrlDialog = false
            },
        )
    }
}

@Composable
private fun Instant?.friendlyFetchDate(): String {
    return if (this == null) {
        stringResource(Res.string.settings_fetch_never)
    } else {
        friendlyDate(this)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = { },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                )
            }
        },
        actions = { },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String = "",
    onClick: () -> Unit,
    showProgress: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp)
                .clickable(onClick = onClick, enabled = !showProgress)
                .padding(16.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(4.dp),
        )
        if (showProgress) {
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        } else if (subtitle.isNotEmpty()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

@Composable
private fun BaseUrlDialog(
    show: Boolean,
    url: String,
    onUrlSet: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var urlText by remember { mutableStateOf(url) }

    if (show) {
        Dialog(onDismissRequest = { }) {
            Card {
                Column(modifier = modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = urlText,
                        singleLine = true,
                        label = {
                            Text(stringResource(Res.string.settings_url))
                        },
                        onValueChange = { urlText = it },
                        modifier = modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = modifier.fillMaxWidth(),
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(Res.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                onUrlSet(urlText)
                            },
                        ) {
                            Text(text = stringResource(Res.string.ok))
                        }
                    }
                }
            }
        }
    }
}
