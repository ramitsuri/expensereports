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
            ignoredExpenseAccounts = IgnoredExpenseAccounts(getIgnoredExpenseAccounts()),
            serverUrl = ServerUrl(getServerUrl())
        )
    )
    val state: StateFlow<SettingsViewState> = _state

    fun setIgnoredExpenseAccounts(ignoredAccounts: List<String>) {
        prefManager.setIgnoredExpenseAccounts(ignoredAccounts)
        _state.update { settingsViewState ->
            settingsViewState.copy(
                ignoredExpenseAccounts = settingsViewState.ignoredExpenseAccounts.copy(
                    accounts = ignoredAccounts
                )
            )
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

    private fun getServerUrl() = prefManager.getServerUrl()
}

data class SettingsViewState(
    val ignoredExpenseAccounts: IgnoredExpenseAccounts = IgnoredExpenseAccounts(),
    val serverUrl: ServerUrl = ServerUrl()
)

data class IgnoredExpenseAccounts(val accounts: List<String> = listOf())

data class ServerUrl(val url: String = "")