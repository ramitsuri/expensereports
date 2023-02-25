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
            ignoredExpenseAccounts = getIgnoredExpenseAccounts(),
            assetAccounts = getAssetAccounts(),
            liabilityAccounts = getLiabilityAccounts(),
            serverUrl = ServerUrl(getServerUrl()),
            downloadViewState = DownloadViewState(lastDownloadTime = getDownloadTime())
        )
    )
    val state: StateFlow<SettingsViewState> = _state

    fun setIgnoredExpenseAccounts(ignoredAccounts: List<String>) {
        prefManager.setIgnoredExpenseAccounts(ignoredAccounts)
        _state.update { previousState ->
            previousState.copy(ignoredExpenseAccounts = ignoredAccounts)
        }
    }

    fun setLiabilityAccounts(liabilityAccounts: List<String>) {
        prefManager.setLiabilityAccounts(liabilityAccounts)
        _state.update { previousState ->
            previousState.copy(liabilityAccounts = liabilityAccounts)
        }
    }

    fun setAssetAccounts(assetAccounts: List<String>) {
        prefManager.setAssetAccounts(assetAccounts)
        _state.update { previousState ->
            previousState.copy(assetAccounts = assetAccounts)
        }
    }

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

    private fun getIgnoredExpenseAccounts(): List<String> {
        return prefManager.getIgnoredExpenseAccounts()
    }

    private fun getLiabilityAccounts(): List<String> {
        return prefManager.getLiabilityAccounts()
    }

    private fun getAssetAccounts(): List<String> {
        return prefManager.getAssetAccounts()
    }

    private fun getServerUrl() = prefManager.getServerUrl()
}

data class SettingsViewState(
    val ignoredExpenseAccounts: List<String> = listOf(),
    val assetAccounts: List<String> = listOf(),
    val liabilityAccounts: List<String> = listOf(),
    val serverUrl: ServerUrl = ServerUrl(),
    val downloadViewState: DownloadViewState
)

data class ServerUrl(val url: String = "")

data class DownloadViewState(
    val isLoading: Boolean = false,
    val lastDownloadTime: LocalDateTime? = null
)