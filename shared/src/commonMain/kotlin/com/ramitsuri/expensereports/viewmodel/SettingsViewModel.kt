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
            ignoredExpenseAccounts = IgnoredExpenseAccounts(getIgnoredExpenseAccounts())
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

    private fun getIgnoredExpenseAccounts(): List<String> {
        return prefManager.getIgnoredExpenseAccounts()
    }
}

data class SettingsViewState(
    val ignoredExpenseAccounts: IgnoredExpenseAccounts = IgnoredExpenseAccounts()
)

data class IgnoredExpenseAccounts(val accounts: List<String> = listOf())