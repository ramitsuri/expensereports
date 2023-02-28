package com.ramitsuri.expensereports.viewmodel

import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.utils.ReportsDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefManager: PrefManager,
    private val downloader: ReportsDownloader
) : ViewModel(), KoinComponent {

    private val timeZone = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        SettingsViewState(
            serverUrl = ServerUrl(getServerUrl()),
            downloadViewState = DownloadViewState(lastDownloadTime = getDownloadTime())
        )
    )
    val state: StateFlow<SettingsViewState> = _state

    fun setServerUrl(url: String) {
        prefManager.setServerUrl(url)
        _state.update {
            it.copy(serverUrl = ServerUrl(url))
        }
    }

    fun downloadReports() {
        _state.update {
            it.copy(downloadViewState = it.downloadViewState.copy(isLoading = true))
        }
        viewModelScope.launch {
            downloader.downloadAndSaveAll()
            _state.update {
                it.copy(
                    downloadViewState = it.downloadViewState.copy(
                        isLoading = false,
                        lastDownloadTime = getDownloadTime()
                    )
                )
            }
        }
    }

    private fun getDownloadTime(): LocalDateTime? {
        return prefManager.getLastDownloadTime()?.toLocalDateTime(timeZone)
    }
    private fun getServerUrl() = prefManager.getServerUrl()
}

data class SettingsViewState(
    val serverUrl: ServerUrl = ServerUrl(),
    val downloadViewState: DownloadViewState
)

data class ServerUrl(val url: String = "")

data class DownloadViewState(
    val isLoading: Boolean = false,
    val lastDownloadTime: LocalDateTime? = null
)