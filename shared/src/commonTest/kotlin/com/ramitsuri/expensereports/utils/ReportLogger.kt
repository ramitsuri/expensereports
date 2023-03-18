package com.ramitsuri.expensereports.utils

fun printAccounts(
    total: SimpleAccountTotal,
    accountTotals: List<SimpleAccountTotal>
) {
    var text = ""
    // Top row
    text += "|Name|"
    for ((month, _) in total.monthAmounts) {
        text += "Month $month|"
    }
    text += "Total|"

    text += "\n"

    // Divider row
    text += "|---|"
    repeat(total.monthAmounts.size) {
        text += "---|"
    }
    text += "---|"
    text += "\n"

    // Total row
    text += printRow(total)

    // Account rows
    accountTotals.forEach { accountTotal ->
        text += printRow(accountTotal)
    }

    print(text)
}


private fun printRow(accountTotal: SimpleAccountTotal): String {
    var prefix = ""
    if (accountTotal.level > 0) {
        prefix += "+"
        repeat(accountTotal.level) {
            prefix += "-"
        }
    }
    var text = "|"
    text += "$prefix${accountTotal.name}"
    text += "|"

    for ((_, balance) in accountTotal.monthAmounts) {
        text += balance.toStringExpanded()
        text += "|"
    }
    text += accountTotal.total.toStringExpanded()
    text += "|\n"
    return text
}
