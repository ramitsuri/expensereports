package com.ramitsuri.expensereports.viewmodel

import com.ramitsuri.expensereports.data.prefs.PrefManager
import com.ramitsuri.expensereports.utils.DataDownloader

class SettingsCallbackViewModel(
    prefManager: PrefManager,
    downloader: DataDownloader
) : CallbackViewModel() {
    override val viewModel = SettingsViewModel(
        prefManager = prefManager,
        downloader = downloader
    )

    val state = viewModel.state.asCallbacks()

    fun setServerUrl(url: String) {
        viewModel.setServerUrl(url)
    }

    fun downloadReports() {
        viewModel.downloadReports()
    }

    fun setShouldDownloadRecentData() {
        viewModel.setShouldDownloadRecentData()
    }
}