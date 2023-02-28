package com.ramitsuri.expensereports.data

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.network.AccountTotalDto
import com.ramitsuri.expensereports.network.BigDecimalSerializer
import com.ramitsuri.expensereports.network.ConfigDto
import com.ramitsuri.expensereports.network.IntBigDecimalMapSerializer
import com.ramitsuri.expensereports.network.ReportDto
import com.ramitsuri.expensereports.utils.Constants
import kotlinx.datetime.Instant
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    val name: String,
    val generatedAt: Instant,
    val fetchedAt: Instant,
    val accountTotal: AccountTotal,
    val type: ReportType,
    val year: Int
) {
    constructor(
        dto: ReportDto,
        fetchedAt: Instant,
        withTotal: Boolean,
        type: ReportType,
        year: Int
    ) : this(
        name = dto.name,
        generatedAt = dto.time,
        fetchedAt = fetchedAt,
        accountTotal = if (withTotal) {
            AccountTotalWithTotal(dto.accountTotal)
        } else {
            AccountTotalWithoutTotal(dto.accountTotal)
        },
        type = type,
        year = year
    )
}

fun Report.isStale(now: Instant): Boolean {
    return now.minus(fetchedAt).inWholeMilliseconds >= Constants.REFRESH_THRESHOLD_MS
}

@Serializable
data class AccountTotalWithTotal(
    @SerialName("name")
    override val name: String,

    @SerialName("fullName")
    override val fullName: String,

    @SerialName("children")
    override val children: List<AccountTotalWithTotal>,

    @Serializable(with = IntBigDecimalMapSerializer::class)
    @SerialName("monthAmounts")
    override val monthAmounts: Map<Int, @Contextual BigDecimal>,

    @Serializable(with = BigDecimalSerializer::class)
    @SerialName("total")
    val total: BigDecimal = BigDecimal.ZERO
) : AccountTotal {
    constructor(dto: AccountTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotalWithTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) }),
        total = BigDecimal.parseString(dto.total)
    )
}

@Serializable
data class AccountTotalWithoutTotal(
    @SerialName("name")
    override val name: String,

    @SerialName("fullName")
    override val fullName: String,

    @SerialName("children")
    override val children: List<AccountTotalWithoutTotal>,

    @Serializable(with = IntBigDecimalMapSerializer::class)
    @SerialName("monthAmounts")
    override val monthAmounts: Map<Int, @Contextual BigDecimal>
) : AccountTotal {
    constructor(dto: AccountTotalDto) : this(
        dto.name,
        dto.fullName,
        dto.children.map { AccountTotalWithoutTotal(it) },
        dto.balances.associateBy(
            keySelector = { it.month },
            valueTransform = { BigDecimal.parseString(it.amount) })
    )
}

@Serializable
sealed interface AccountTotal {
    val name: String
    val fullName: String
    val children: List<AccountTotal>
    val monthAmounts: Map<Int, BigDecimal>

    companion object {
        fun fromAccountTotalAndChildren(
            accountTotal: AccountTotal,
            children: List<AccountTotal>
        ): AccountTotal {
            var total = BigDecimal.ZERO
            val monthAmounts = if (children.isEmpty()) {
                accountTotal.monthAmounts
            } else {
                val monthAmounts = mutableMapOf<Int, BigDecimal>()
                for (month in (1..12)) {
                    var monthTotal = BigDecimal.ZERO
                    children.forEach { monthTotal += it.monthAmounts[month] ?: BigDecimal.ZERO }
                    monthAmounts[month] = monthTotal
                    total += monthTotal
                }
                monthAmounts
            }
            return if (accountTotal is AccountTotalWithTotal) {
                AccountTotalWithTotal(
                    name = accountTotal.name,
                    fullName = accountTotal.fullName,
                    children = children.map { it as AccountTotalWithTotal },
                    monthAmounts = monthAmounts,
                    total = total
                )
            } else {
                AccountTotalWithoutTotal(
                    name = accountTotal.name,
                    fullName = accountTotal.fullName,
                    children = children.map { it as AccountTotalWithoutTotal },
                    monthAmounts = monthAmounts
                )
            }

        }
    }
}


fun AccountTotal.isIn(
    selectedAccountNames: List<String>?,
    fullName: Boolean = false
): Boolean {
    if (selectedAccountNames == null) {
        return true
    }
    return if (fullName) {
        selectedAccountNames.contains(this.fullName)
    } else {
        selectedAccountNames.contains(this.name)
    }
}

fun AccountTotal.isNotIn(
    selectedAccountNames: List<String>?,
    fullName: Boolean = false
): Boolean {
    return !isIn(selectedAccountNames, fullName)
}


@Serializable
data class Config(
    val ignoredExpenseAccounts: List<String>,
    val mainAssetAccounts: List<String>,
    val mainLiabilityAccounts: List<String>,
    val mainIncomeAccounts: List<String>,
    @Serializable(with = BigDecimalSerializer::class)
    val annualBudget: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val annualSavingsTarget: BigDecimal
) {
    constructor(dto: ConfigDto) : this(
        ignoredExpenseAccounts = dto.ignoredExpenseAccounts,
        mainAssetAccounts = dto.mainAssetAccounts,
        mainLiabilityAccounts = dto.mainLiabilityAccounts,
        mainIncomeAccounts = dto.mainIncomeAccounts,
        annualBudget = try {
            BigDecimal.parseString(dto.annualBudget)
        } catch (e: Exception) {
            BigDecimal.ZERO
        },
        annualSavingsTarget = try {
            BigDecimal.parseString(dto.annualSavingsTarget)
        } catch (e: Exception) {
            BigDecimal.ZERO
        },
    )
}