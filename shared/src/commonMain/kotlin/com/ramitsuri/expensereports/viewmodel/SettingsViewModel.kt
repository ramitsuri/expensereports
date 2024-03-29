package com.ramitsuri.expensereports.viewmodel

import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.utils.DataDownloader
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefManager: PrefManager,
    private val downloader: DataDownloader
) : ViewModel(), KoinComponent {

    private val timeZone = TimeZone.currentSystemDefault()

    private val _state = MutableStateFlow(
        SettingsViewState(
            serverUrl = ServerUrl(getServerUrl()),
            downloadViewState = DownloadViewState(lastDownloadTime = getDownloadTime()),
            shouldDownloadRecentData = shouldDownloadRecentData()
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
            downloader.download()
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

    fun setShouldDownloadRecentData() {
        val currentValue = _state.value.shouldDownloadRecentData
        val newValue = !currentValue
        prefManager.setDownloadRecentData(newValue)
        _state.update {
            it.copy(shouldDownloadRecentData = newValue)
        }
    }

    private fun getDownloadTime(): LocalDateTime? {
        return prefManager.getLastDownloadTime()?.toLocalDateTime(timeZone)
    }

    private fun getServerUrl() = prefManager.getServerUrl()

    private fun shouldDownloadRecentData() = prefManager.shouldDownloadRecentData()
}

data class SettingsViewState(
    val serverUrl: ServerUrl = ServerUrl(),
    val downloadViewState: DownloadViewState,
    val shouldDownloadRecentData: Boolean
)

data class ServerUrl(val url: String = "")

data class DownloadViewState(
    val isLoading: Boolean = false,
    val lastDownloadTime: LocalDateTime? = null
)