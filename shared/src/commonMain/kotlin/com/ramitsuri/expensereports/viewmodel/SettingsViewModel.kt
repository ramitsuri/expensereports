package com.ramitsuri.expensereports.viewmodel

import com.ramitsuri.expensereports.data.prefs.PrefManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinComponent

class SettingsViewModel(
    private val prefManager: PrefManager
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(
        SettingsViewState(
            ignoredExpenseAccounts = getIgnoredExpenseAccounts(),
            assetAccounts = getAssetAccounts(),
            liabilityAccounts = getLiabilityAccounts(),
            serverUrl = ServerUrl(getServerUrl())
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
    val serverUrl: ServerUrl = ServerUrl()
)

data class ServerUrl(val url: String = "")