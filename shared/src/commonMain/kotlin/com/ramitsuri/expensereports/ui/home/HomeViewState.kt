package com.ramitsuri.expensereports.ui.home

import com.ramitsuri.expensereports.model.MonthYear
import java.math.BigDecimal

data class HomeViewState(
    val expandableCardGroups: List<ExpandableCardGroup> = listOf(),
    val netWorths: List<NetWorth> = listOf(),
    val selectedNetWorthPeriod: Period,
    val periods: List<Period> = listOf(),
    val refreshState: Refresh = Refresh(),
) {
    data class ExpandableCardGroup(
        val name: String,
        val value: String,
        val isValuePositive: Boolean,
        val children: List<Child>,
    ) {
        data class Child(
            val title: String,
            val value: String,
        )
    }

    data class NetWorth(
        val monthYear: MonthYear,
        val netWorth: BigDecimal,
    )

    data class Refresh(
        val isRefreshing: Boolean = false,
        val isPullToRefreshAvailable: Boolean = true,
    )

    sealed interface Period {
        data object ThisYear : Period

        data object OneYear : Period

        data object LastThreeYears : Period

        data object AllTime : Period
    }
}
