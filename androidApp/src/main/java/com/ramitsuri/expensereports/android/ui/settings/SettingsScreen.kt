package com.ramitsuri.expensereports.android.ui.settings

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.extensions.shutdown
import com.ramitsuri.expensereports.android.utils.timeDateMonthYear
import com.ramitsuri.expensereports.viewmodel.DownloadViewState
import com.ramitsuri.expensereports.viewmodel.SettingsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = getViewModel(),
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    onBack: () -> Unit
) {
    val viewState = viewModel.state.collectAsState().value

    SettingsContent(
        snackbarHostState = snackbarHostState,
        serverUrl = viewState.serverUrl.url,
        onUrlSet = viewModel::setServerUrl,
        downloadViewState = viewState.downloadViewState,
        onDownloadClicked = viewModel::downloadReports,
        onBack = onBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsContent(
    snackbarHostState: SnackbarHostState,
    serverUrl: String,
    onUrlSet: (String) -> Unit,
    downloadViewState: DownloadViewState,
    onDownloadClicked: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { },
                navigationIcon = {
                    IconButton(
                        onClick = onBack
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .displayCutoutPadding(),
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(horizontal = 8.dp)
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
