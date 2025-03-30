package com.ramitsuri.expensereports.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.settings.Settings
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settings: Settings,
) : ViewModel() {
    val viewState = combine(
        settings.getBaseUrlFlow(),
        settings.getLastFetchTimeFlow(),
        settings.getLastFullFetchTimeFlow(),
    ) { baseUrl, lastFetchTime, lastFullFetchTime ->
        SettingsViewState(
            url = baseUrl,
            lastFetchTime = lastFetchTime,
            lastFullFetchTime = lastFullFetchTime,
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
}
