package com.ramitsuri.expensereports.ui.report

import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportForTable
import com.ramitsuri.expensereports.model.ReportNames

data class ReportViewState(
    val selectedReport: ReportNames,
    val selectedPeriod: Period,
    val report: ReportForTable? = null,
    val isLoading: Boolean = true,
)
