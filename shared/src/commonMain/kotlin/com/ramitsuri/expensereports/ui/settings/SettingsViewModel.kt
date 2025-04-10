package com.ramitsuri.expensereports.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.settings.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: Settings,
) : ViewModel() {
    val viewState =
        combine(
            settings.getBaseUrlFlow(),
            timesFlow,
        ) { baseUrl, times ->
            SettingsViewState(
                url = baseUrl,
                times = times,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SettingsViewState(),
        )

    fun setBaseUrl(url: String) {
        viewModelScope.launch {
            settings.setBaseUrl(url)
        }
    }

    private val timesFlow: Flow<SettingsViewState.Times>
        get() =
            combine(
                settings.getLastFetchTimeFlow(),
                settings.getLastFullFetchTimeFlow(),
                settings.getRunInfoFlow(),
            ) { lastFetchTime, lastFullFetchTime, runInfo ->
                SettingsViewState.Times(
                    lastFetchTime = lastFetchTime,
                    lastFullFetchTime = lastFullFetchTime,
                    fileLastModifiedTime = runInfo?.fileLastModifiedTime,
                    lastRunTime = runInfo?.lastRunTime,
                )
            }
}
