package com.ramitsuri.expensereports.android.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.extensions.shutdown
import com.ramitsuri.expensereports.utils.timeDateMonthYear
import com.ramitsuri.expensereports.viewmodel.DownloadViewState
import com.ramitsuri.expensereports.viewmodel.SettingsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value

    SettingsContent(
        serverUrl = viewState.serverUrl.url,
        onUrlSet = viewModel::setServerUrl,
        downloadViewState = viewState.downloadViewState,
        onDownloadClicked = viewModel::downloadReports,
        shouldDownloadRecent = viewState.shouldDownloadRecentData,
        onShouldDownloadRecentClicked = viewModel::setShouldDownloadRecentData
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    serverUrl: String,
    onUrlSet: (String) -> Unit,
    downloadViewState: DownloadViewState,
    onDownloadClicked: () -> Unit,
    shouldDownloadRecent: Boolean,
    onShouldDownloadRecentClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                ServerUrlItem(
                    serverUrl = serverUrl,
                    onUrlSet = onUrlSet
                )
            }
            item {
                DownloadReportsItem(
                    downloadViewState = downloadViewState,
                    onClick = onDownloadClicked
                )
            }
            item {
                ShouldDownloadRecentItem(
                    shouldDownloadRecent = shouldDownloadRecent,
                    onClick = onShouldDownloadRecentClicked
                )
            }
        }
    }
}

@Composable
fun ServerUrlItem(
    serverUrl: String,
    onUrlSet: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dialogState = rememberSaveable { mutableStateOf(false) }
    var serverSet by rememberSaveable { mutableStateOf(false) }
    val subtitle = if (serverSet) {
        stringResource(id = R.string.settings_server_url_restart)
    } else if (serverUrl.isEmpty()) {
        stringResource(id = R.string.settings_server_url_server_not_set)
    } else {
        serverUrl
    }
    SettingsItem(
        title = stringResource(id = R.string.settings_server_url_title),
        subtitle = subtitle,
        onClick = {
            if (serverSet) {
                context.shutdown()
            } else {
                dialogState.value = true
            }
        },
        modifier = modifier
    )
    if (dialogState.value) {
        SetApiUrlDialog(
            previousUrl = serverUrl,
            onPositiveClick = { url ->
                dialogState.value = false
                onUrlSet(url)
                serverSet = true
            },
            onNegativeClick = {
                dialogState.value = false
            },
            dialogState = dialogState
        )
    }
}

@Composable
fun DownloadReportsItem(
    downloadViewState: DownloadViewState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val downloadTime = downloadViewState.lastDownloadTime
    SettingsItem(
        title = stringResource(id = R.string.settings_download_title),
        subtitle = if (downloadTime == null) {
            stringResource(id = R.string.settings_download_never_downloaded)
        } else {
            stringResource(
                id = R.string.settings_download_last_download_time_format,
                downloadTime.timeDateMonthYear()
            )
        },
        onClick = onClick,
        showProgress = downloadViewState.isLoading,
        modifier = modifier
    )
}

@Composable
fun ShouldDownloadRecentItem(
    shouldDownloadRecent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    SettingsItemWithSwitch(
        title = stringResource(id = R.string.settings_should_download_recent_title),
        subtitle = stringResource(id = R.string.settings_should_download_recent_subtitle),
        checked = shouldDownloadRecent,
        onClick = onClick,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SetApiUrlDialog(
    previousUrl: String,
    onPositiveClick: (String) -> Unit,
    onNegativeClick: () -> Unit,
    modifier: Modifier = Modifier,
    dialogState: MutableState<Boolean>
) {
    var text by rememberSaveable { mutableStateOf(previousUrl.ifEmpty { "http://" }) }
    Dialog(
        onDismissRequest = { dialogState.value = false },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Card {
            Column(modifier = modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = text,
                    singleLine = true,
                    onValueChange = { text = it },
                    modifier = modifier.fillMaxWidth()
                )
                Spacer(modifier = modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        onNegativeClick()
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        onPositiveClick(text)
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showProgress: Boolean = false
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(onClick = onClick, enabled = true)
            .padding(12.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            modifier = modifier.padding(4.dp)
        )
        if (showProgress) {
            Spacer(modifier = modifier.height(8.dp))
            LinearProgressIndicator(modifier = modifier.fillMaxWidth())
        } else {
            Text(
                text = subtitle,
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                modifier = modifier
                    .padding(horizontal = 4.dp)
                    .basicMarquee()
            )
        }
    }
}

@Composable
private fun SettingsItemWithSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = modifier.padding(4.dp)
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                modifier = modifier.padding(horizontal = 4.dp)
            )
        }

        val icon: (@Composable () -> Unit)? = if (checked) {
            {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(SwitchDefaults.IconSize),
                )
            }
        } else {
            null
        }
        Switch(
            checked = checked,
            onCheckedChange = { onClick() },
            thumbContent = icon
        )
    }
}
