package com.ramitsuri.expensereports.android.ui.reports

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.ui.components.LineChart
import com.ramitsuri.expensereports.android.ui.components.LineChartValue
import com.ramitsuri.expensereports.android.ui.views.AccountsFilterDialog
import com.ramitsuri.expensereports.android.ui.views.Table
import com.ramitsuri.expensereports.android.ui.views.TableCell
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.timeAndDay
import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.data.ReportType
import com.ramitsuri.expensereports.ui.Account
import com.ramitsuri.expensereports.ui.FilterItem
import com.ramitsuri.expensereports.utils.ReportView
import com.ramitsuri.expensereports.utils.SimpleAccountTotal
import com.ramitsuri.expensereports.viewmodel.ReportSelection
import com.ramitsuri.expensereports.viewmodel.ReportsViewModel
import com.ramitsuri.expensereports.viewmodel.Selector
import com.ramitsuri.expensereports.viewmodel.View
import com.ramitsuri.expensereports.viewmodel.ViewType
import com.ramitsuri.expensereports.viewmodel.Year
import kotlinx.datetime.Instant
import org.koin.androidx.compose.getViewModel

@Composable
fun ReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: ReportsViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value

    ReportsContent(
        isLoading = viewState.loading,
        error = viewState.error,
        years = viewState.years,
        onYearSelected = viewModel::yearSelected,
        reportTypes = viewState.reports,
        onReportTypeSelected = viewModel::reportTypeSelected,
        views = viewState.views,
        onViewSelected = viewModel::onViewSelected,
        onErrorShown = viewModel::onErrorShown,
        accounts = viewState.accounts,
        onAccountFiltersApplied = viewModel::onAccountFiltersApplied,
        months = viewState.months,
        onMonthClicked = viewModel::onMonthClicked,
        reportView = viewState.report,
        modifier = modifier
    )
}

@Composable
private fun ReportsContent(
    isLoading: Boolean,
    error: Error?,
    years: List<Year>,
    onYearSelected: (Year) -> Unit,
    reportTypes: List<ReportSelection>,
    onReportTypeSelected: (ReportSelection) -> Unit,
    views: List<View>,
    onViewSelected: (View) -> Unit,
    onErrorShown: () -> Unit,
    accounts: List<Account>,
    onAccountFiltersApplied: (List<Account>) -> Unit,
    months: List<FilterItem>,
    onMonthClicked: (item: FilterItem) -> Unit,
    reportView: ReportView?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
        } else {
            if (error != null) {
                Toast.makeText(
                    LocalContext.current,
                    error.toString(),
                    Toast.LENGTH_LONG
                ).show()
                onErrorShown()
            }
            TopRow(
                years = years,
                onYearSelected = onYearSelected,
                views = views,
                onViewSelected = onViewSelected,
                reportTypes = reportTypes,
                onReportTypeSelected = onReportTypeSelected
            )
            FilterRow(
                accounts = accounts,
                onAccountFiltersApplied = onAccountFiltersApplied,
                items = months,
                onItemClicked = onMonthClicked
            )

            if (reportView == null) {
                ReportUnavailable()
            } else {
                ReportView(report = reportView)
            }
        }
    }
}

@Composable
private fun ReportView(
    report: ReportView
) {
    when (report) {
        is ReportView.Full -> {
            TableView(
                report.accountTotals,
                report.total,
                report.total.monthAmounts.map { it.key },
                report.generatedAt
            )
        }

        is ReportView.ByMonth -> {
            Chart(report.monthTotals, report.total)
        }

        else -> {
            ReportUnavailable()
        }
    }
}

@Composable
private fun Chart(months: Map<Int, BigDecimal>, total: BigDecimal) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(id = R.string.total_format, total.format()),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        LineChart(
            balances = months.map { (month, amount) ->
                LineChartValue(label = stringResource(id = month.string()), numericValue = amount)
            },
            color = MaterialTheme.colorScheme.onBackground,
            formatNumericValueRounded = false,
        )
    }
}

@Composable
private fun TableView(
    accountTotals: List<SimpleAccountTotal>,
    total: SimpleAccountTotal,
    sortedMonths: List<Int>,
    generatedAt: Instant
) {
    val rows = accountTotals.size + 2
    val columns = sortedMonths.size.plus(2)
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(text = generatedAt.timeAndDay(), style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Table(
            modifier = Modifier.fillMaxSize(),
            columnCount = columns,
            rowCount = rows,
            cellContent = { columnIndex, rowIndex ->
                when (rowIndex) {
                    0 -> { // Header row
                        when (columnIndex) {
                            0 -> {
                                TableCell(
                                    text = stringResource(id = R.string.accounts),
                                    isHeader = true
                                )
                            }

                            columns - 1 -> {
                                TableCell(
                                    text = stringResource(id = R.string.total),
                                    isHeader = true
                                )
                            }

                            else -> {
                                TableCell(
                                    text = stringResource(id = sortedMonths[columnIndex - 1].string()),
                                    isHeader = true
                                )
                            }
                        }
                    }

                    1 -> { // Totals Account row
                        when (columnIndex) {
                            0 -> {
                                TableCell(
                                    text = total.name,
                                    isHeader = true
                                )
                            }

                            columns - 1 -> {
                                TableCell(text = total.total.format(), isHeader = true)
                            }

                            else -> {
                                val month = sortedMonths[columnIndex - 1]
                                TableCell(
                                    text = (total.monthAmounts[month] ?: BigDecimal.ZERO).format(),
                                    isHeader = true
                                )
                            }
                        }
                    }

                    else -> { // Other Accounts
                        val account = accountTotals[rowIndex - 2]
                        when (columnIndex) {
                            0 -> {
                                var prefix = ""
                                repeat(account.level - 1) {
                                    prefix += "   "
                                }
                                TableCell(text = prefix + account.name, isHeader = true)
                            }

                            columns - 1 -> {
                                TableCell(
                                    text = account.total.format(),
                                    isHeader = true
                                )
                            }

                            else -> {
                                val month = sortedMonths[columnIndex - 1]
                                TableCell(
                                    text = (account.monthAmounts[month]
                                        ?: BigDecimal.ZERO).format(),
                                    isHeader = false
                                )
                            }
                        }
                    }
                }
            })
    }
}

@Composable
private fun ReportUnavailable(modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
    ) {
        Text(text = stringResource(id = R.string.report_unavailable))
    }
}

@Composable
private fun TopRow(
    years: List<Year>,
    onYearSelected: (Year) -> Unit,
    views: List<View>,
    onViewSelected: (View) -> Unit,
    reportTypes: List<ReportSelection>,
    onReportTypeSelected: (ReportSelection) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        item {
            Selector(
                items = reportTypes,
                onItemSelected = { selector ->
                    onReportTypeSelected(selector as ReportSelection)
                },
                contentDescription = R.string.report_type_selector_content_description,
                modifier = Modifier
            )
        }
        item {
            Selector(
                items = years,
                onItemSelected = { selector ->
                    onYearSelected(selector as Year)
                },
                contentDescription = R.string.report_year_selector_content_description,
                modifier = Modifier
            )
        }
        item {
            Selector(
                items = views,
                onItemSelected = { selector ->
                    onViewSelected(selector as View)
                },
                contentDescription = R.string.report_view_selector_content_description,
                modifier = Modifier
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Selector(
    items: List<Selector>,
    onItemSelected: (Selector) -> Unit,
    @StringRes contentDescription: Int,
    modifier: Modifier,
) {
    @Composable
    fun Selector.name(): String {
        return when (this) {
            is ReportSelection -> type.string()
            is View -> type.string()
            is Year -> year.toString()
        }
    }

    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = items.any { it.selected },
            onClick = { expanded = !expanded },
            label = { Text(items.first { it.selected }.name()) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(id = contentDescription),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            items.forEach { view ->
                DropdownMenuItem(
                    text = {
                        Text(view.name())
                    },
                    onClick = {
                        expanded = false
                        onItemSelected(view)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    accounts: List<Account>,
    onAccountFiltersApplied: (List<Account>) -> Unit,
    items: List<FilterItem>,
    onItemClicked: (item: FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val dialogState = rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        FilterChip(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.KeyboardArrowDown,
                    contentDescription = stringResource(id = R.string.accounts),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            },
            selected = true,
            onClick = { dialogState.value = !dialogState.value },
            label = { Text(stringResource(id = R.string.accounts)) }
        )
        Spacer(modifier = Modifier.width(8.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items.forEach { filterItem ->
                item {
                    FilterChip(
                        selected = filterItem.selected,
                        onClick = { onItemClicked(filterItem) },
                        label = { Text(filterItem.displayName) }
                    )
                }
            }
        }
    }
    if (dialogState.value) {
        AccountsFilterDialog(
            accounts = accounts,
            onAccountFiltersApplied = onAccountFiltersApplied,
            dialogState = dialogState
        )
    }
}

@Composable
fun ViewType.string(): String {
    val stringRes = when (this) {
        ViewType.TABLE -> R.string.report_view_table
        ViewType.CHART -> R.string.report_view_chart
    }
    return stringResource(id = stringRes)
}

@Composable
fun ReportType.string(): String {
    val stringRes = when (this) {
        ReportType.EXPENSE -> {
            R.string.report_type_expense
        }

        ReportType.EXPENSE_AFTER_DEDUCTION -> {
            R.string.report_type_expense_after_deduction
        }

        ReportType.ASSETS -> {
            R.string.report_type_assets
        }

        ReportType.LIABILITIES -> {
            R.string.report_type_liabilities
        }

        ReportType.INCOME -> {
            R.string.report_type_income
        }

        ReportType.NET_WORTH -> {
            R.string.report_type_net_worth
        }

        ReportType.SAVINGS -> {
            R.string.report_type_savings
        }

        ReportType.NONE -> {
            R.string.report_type_none
        }
    }
    return stringResource(id = stringRes)
}

@StringRes
fun Int.string(): Int {
    return when (this) {
        1 ->
            R.string.header_jan

        2 ->
            R.string.header_feb

        3 ->
            R.string.header_mar

        4 ->
            R.string.header_apr

        5 ->
            R.string.header_may

        6 ->
            R.string.header_jun

        7 ->
            R.string.header_jul

        8 ->
            R.string.header_aug

        9 ->
            R.string.header_sep

        10 ->
            R.string.header_oct

        11 ->
            R.string.header_nov

        12 ->
            R.string.header_dec

        13 ->
            R.string.header_total

        else ->
            R.string.header_invalid
    }
}