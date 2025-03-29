package com.ramitsuri.expensereports.ui.home

import com.ramitsuri.expensereports.model.CurrentBalance
import com.ramitsuri.expensereports.model.MonthYear

data class HomeViewState(
    val currentBalanceGroups: List<CurrentBalanceGroup> = listOf(),
    val netWorths: List<NetWorth> = listOf(),
    val selectedNetWorthPeriod: Period,
    val selectedSavingsRatePeriod: Period,
    val savingsRates: List<SavingsRate> = listOf(),
) {
    data class CurrentBalanceGroup(
        val groupName: String? = null,
        val groupTotal: String? = null,
        val currentBalances: List<CurrentBalance>,
    )

    data class NetWorth(
        val monthYear: MonthYear,
        val netWorth: String,
    )

    data class SavingsRate(
        val monthYear: MonthYear,
        val savingsRate: String,
    )

    sealed interface Period {
        data object ThisYear : Period
        data object OneYear : Period
        data object LastThreeYears : Period
        data object AllTime : Period
    }
}
