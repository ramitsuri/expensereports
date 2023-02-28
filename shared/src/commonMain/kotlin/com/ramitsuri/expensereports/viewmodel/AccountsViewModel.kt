package com.ramitsuri.expensereports.viewmodel

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.data.isIn
import com.ramitsuri.expensereports.repository.ConfigRepository
import com.ramitsuri.expensereports.repository.ReportsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class AccountsViewModel(
    private val repository: ReportsRepository,
    private val configRepository: ConfigRepository,
    clock: Clock
) : ViewModel() {

    private val timeZone = TimeZone.currentSystemDefault()
    private val now = clock.now().toLocalDateTime(timeZone)

    private val _state: MutableStateFlow<AccountsViewState> = MutableStateFlow(AccountsViewState())
    val state: StateFlow<AccountsViewState> = _state

    init {
        val month = now.monthNumber
        val years = listOf(now.year)
        val reportTypes = listOf(reportTypeAssets, reportTypeLiabilities)

        viewModelScope.launch {
            val reports = repository.get(years, reportTypes)

            // Liability accounts
            val liabilityReport = reports.firstOrNull { it.type == reportTypeLiabilities }
            if (liabilityReport != null) {
                val accountBalances = getFilteredAccounts(
                    accountTotal = liabilityReport.accountTotal,
                    month = month,
                    includeAccounts = configRepository.getLiabilityAccounts()
                )
                val liabilityAccount = Account(
                    asOf = liabilityReport.generatedAt.toLocalDateTime(timeZone),
                    accountBalances = accountBalances
                )
                _state.update { previousState ->
                    val map = previousState.accounts.toMutableMap()
                    map[AccountType.LIABILITY] = liabilityAccount
                    previousState.copy(accounts = map)
                }
            }

            // Asset accounts
            val assetReport = reports.firstOrNull { it.type == reportTypeAssets }
            if (assetReport != null) {
                val accountBalances = getFilteredAccounts(
                    accountTotal = assetReport.accountTotal,
                    month = month,
                    includeAccounts = configRepository.getAssetAccounts()
                )
                val assetAccount = Account(
                    asOf = assetReport.generatedAt.toLocalDateTime(timeZone),
                    accountBalances = accountBalances
                )
                _state.update { previousState ->
                    val map = previousState.accounts.toMutableMap()
                    map[AccountType.ASSET] = assetAccount
                    previousState.copy(accounts = map)
                }
            }
        }
    }

    private fun getFilteredAccounts(
        accountTotal: AccountTotal,
        month: Int,
        includeAccounts: List<String>
    ): List<AccountBalance> {
        val result = mutableListOf<AccountBalance>()
        if (accountTotal.isIn(includeAccounts, fullName = true)) {
            val balance = accountTotal.monthAmounts[month] ?: BigDecimal.ZERO
            result.add(AccountBalance(name = accountTotal.name, balance = balance))
        }
        for (childAccountTotal in accountTotal.children) {
            result.addAll(getFilteredAccounts(childAccountTotal, month, includeAccounts))
        }
        return result
    }

    companion object {
        private val reportTypeAssets = ReportType.ASSETS
        private val reportTypeLiabilities = ReportType.LIABILITIES
    }

}

data class AccountsViewState(
    val isLoading: Boolean = false,
    val accounts: Map<AccountType, Account> = mapOf(),
    val asOf: LocalDateTime? = null
)

data class Account(
    val asOf: LocalDateTime,
    val accountBalances: List<AccountBalance>
)

data class AccountBalance(val name: String, val balance: BigDecimal)

enum class AccountType {
    ASSET,
    LIABILITY
}