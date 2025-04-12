package com.ramitsuri.expensereports.ui.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportForTable
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.repository.MainRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone

class ReportViewModel(
    mainRepository: MainRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {
    private val reportName: MutableStateFlow<ReportNames> = MutableStateFlow(ReportNames.AfterDeductionsExpenses)
    private val period: MutableStateFlow<Period> = MutableStateFlow(Period.ThisYear)

    @OptIn(ExperimentalCoroutinesApi::class)
    val viewState =
        combine(
            reportName,
            period,
        ) { reportName, period ->
            reportName to period
        }.flatMapLatest { (reportName, period) ->
            mainRepository.getReport(
                reportName = reportName.name,
                monthYears = period.toMonthYears(MonthYear.now(clock, timeZone)),
            ).map { report ->
                ReportViewState(
                    selectedReport = reportName,
                    selectedPeriod = period,
                    report = report?.let { ReportForTable.fromReport(it) },
                    isLoading = false,
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue =
                ReportViewState(
                    selectedReport = reportName.value,
                    selectedPeriod = period.value,
                ),
        )

    fun onReportSelected(reportName: ReportNames) {
        this.reportName.value = reportName
    }

    fun onPeriodSelected(period: Period) {
        this.period.value = period
    }
}
