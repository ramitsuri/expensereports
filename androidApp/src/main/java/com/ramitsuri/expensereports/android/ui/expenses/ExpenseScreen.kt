package com.ramitsuri.expensereports.android.ui.expenses

import android.widget.Toast
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.data.AccountTotalWithTotal
import com.ramitsuri.expensereports.data.Error
import com.ramitsuri.expensereports.ui.FilterItem
import com.ramitsuri.expensereports.utils.ExpenseReportView
import com.ramitsuri.expensereports.viewmodel.ExpenseReportViewModel
import com.ramitsuri.expensereports.viewmodel.View
import com.ramitsuri.expensereports.viewmodel.ViewType
import com.ramitsuri.expensereports.viewmodel.Year
import org.koin.androidx.compose.getViewModel

@Composable
fun ExpensesScreen(
    modifier: Modifier = Modifier,
    viewModel: ExpenseReportViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value

    ExpenseContent(
        isLoading = viewState.loading,
        error = viewState.error,
        years = viewState.years,
        onYearSelected = viewModel::reportSelected,
        views = viewState.views,
        onViewSelected = viewModel::onViewSelected,
        onErrorShown = viewModel::onErrorShown,
        accounts = viewState.accounts,
        onAccountClicked = viewModel::onAccountClicked,
        months = viewState.months,
        onMonthClicked = viewModel::onMonthClicked,
        reportView = viewState.report,
        modifier = modifier
    )
}

@Composable
private fun ExpenseContent(
    isLoading: Boolean,
    error: Error?,
    years: List<Year>,
    onYearSelected: (Year) -> Unit,
    views: List<View>,
    onViewSelected: (View) -> Unit,
    onErrorShown: () -> Unit,
    accounts: List<FilterItem>,
    onAccountClicked: (item: FilterItem) -> Unit,
    months: List<FilterItem>,
    onMonthClicked: (item: FilterItem) -> Unit,
    reportView: ExpenseReportView?,
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
                onViewSelected = onViewSelected
            )
            FilterRow(
                items = accounts,
                onItemClicked = onAccountClicked
            )
            FilterRow(
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
    report: ExpenseReportView
) {
    when (report) {
        is ExpenseReportView.Full -> {
            val context = LocalContext.current
            LaunchedEffect(key1 = report) {
                Toast.makeText(context, report.generatedAt, Toast.LENGTH_SHORT).show()
            }
            TableView(
                report.accountTotals,
                report.total,
                report.total.monthAmounts.map { it.key }
            )
        }

        is ExpenseReportView.ByMonth -> {
            BarChartMonth(report.monthTotals, report.total)
        }

        is ExpenseReportView.ByAccount -> {
            BarChartAccount(report.accountTotals, report.total)
        }
        else -> {
            ReportUnavailable()
        }
    }
}

@Composable
private fun BarChartAccount(accounts: Map<String, BigDecimal>, total: BigDecimal) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Total: ${total.toStringExpanded()}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        if (total != BigDecimal.ZERO) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.weight(0.15f))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                    ) {
                        (1..10).forEach { index ->
                            Row(
                                modifier = Modifier.weight(0.02F)
                            ) {
                                Text(
                                    "${(11 - index) * 10}%",
                                    fontSize = TextUnit(6F, TextUnitType.Sp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Divider(
                                    modifier = Modifier.height(1.dp),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2F)
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.08F))
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.25f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    LazyRow {
                        accounts.forEach { (account, amount) ->
                            item {
                                val value =
                                    amount.divide(total, decimalMode = DecimalMode.US_CURRENCY)
                                        .floatValue(exactRequired = false)
                                BarChartBar(
                                    value = value,
                                    label1 = account,
                                    label2 = amount.toStringExpanded()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarChartMonth(months: Map<Int, BigDecimal>, total: BigDecimal) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = "Total: ${total.toStringExpanded()}",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(8.dp)
        )
        if (total != BigDecimal.ZERO) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.weight(0.15f))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f)
                    ) {
                        (1..10).forEach { index ->
                            Row(
                                modifier = Modifier.weight(0.02F)
                            ) {
                                Text(
                                    "${(11 - index) * 10}%",
                                    fontSize = TextUnit(6F, TextUnitType.Sp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Divider(
                                    modifier = Modifier.height(1.dp),
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2F)
                                )
                            }
                            Spacer(modifier = Modifier.weight(0.08F))
                        }
                    }
                    Spacer(modifier = Modifier.weight(0.25f))
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.width(16.dp))
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        months.forEach { (month, amount) ->
                            item {
                                val value =
                                    amount.divide(total, decimalMode = DecimalMode.US_CURRENCY)
                                        .floatValue(exactRequired = false)
                                BarChartBar(
                                    value = value,
                                    label1 = stringResource(id = month.string()),
                                    label2 = amount.toStringExpanded()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BarChartBar(value: Float, label1: String, label2: String) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .widthIn(min = 0.dp, max = 64.dp)
            .padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.15f))
        Column(
            modifier = Modifier
                .weight(0.6F)
                .width(24.dp)
        ) {
            if (value != 1F) {
                Spacer(
                    modifier = Modifier
                        .weight(1F - value)
                )
            }
            if (value != 0F) {
                Divider(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(value),
                    color = MaterialTheme.colorScheme.primaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.weight(0.05f))
        Column(
            modifier = Modifier.weight(0.2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label1,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.basicMarquee()
            )
            Text(
                text = label2,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Visible,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

@Composable
private fun TableView(
    accountTotals: List<AccountTotalWithTotal>,
    total: AccountTotalWithTotal,
    sortedMonths: List<Int>
) {
    val rows = accountTotals.size + 2
    val columns = sortedMonths.size.plus(2)
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Table(
            modifier = Modifier.matchParentSize(),
            columnCount = columns,
            rowCount = rows,
            cellContent = { columnIndex, rowIndex ->
                when (rowIndex) {
                    0 -> { // Headers
                        when (columnIndex) {
                            0 -> {
                                TableCell(text = "Accounts", isHeader = true)
                            }
                            columns - 1 -> {
                                TableCell(text = "Total", isHeader = true)
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
                                TableCell(text = "Total", isHeader = true)
                            }
                            columns - 1 -> {
                                TableCell(text = total.total.toStringExpanded(), isHeader = true)
                            }
                            else -> {
                                val month = sortedMonths[columnIndex - 1]
                                TableCell(
                                    text = total.monthAmounts[month]?.toStringExpanded() ?: "0.0",
                                    isHeader = true
                                )
                            }
                        }
                    }
                    else -> { // Other Accounts
                        val account = accountTotals[rowIndex - 2]
                        when (columnIndex) {
                            0 -> {
                                TableCell(text = account.name, isHeader = true)
                            }
                            columns - 1 -> {
                                TableCell(
                                    text = account.total.toStringExpanded(),
                                    isHeader = true
                                )
                            }
                            else -> {
                                val month = sortedMonths[columnIndex - 1]
                                TableCell(
                                    text = account.monthAmounts[month]?.toStringExpanded() ?: "0.0",
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
private fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    columnCount: Int,
    rowCount: Int,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }

    Box(modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))) {
        LazyColumn(state = verticalLazyListState) {
            items(rowCount) { rowIndex ->
                Column {
                    Row(
                        modifier = if (rowIndex % 2 == 0) {
                            rowModifier.then(Modifier.background(MaterialTheme.colorScheme.primaryContainer))
                        } else {
                            rowModifier
                        }
                    ) {
                        (0 until columnCount).forEach { columnIndex ->
                            Box(
                                modifier = Modifier
                                    .border(
                                        border = BorderStroke(
                                            1.dp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)

                                        val existingWidth = columnWidths[columnIndex] ?: 0
                                        val maxWidth = maxOf(existingWidth, placeable.width)

                                        if (maxWidth > existingWidth) {
                                            columnWidths[columnIndex] = maxWidth
                                        }

                                        layout(width = maxWidth, height = placeable.height) {
                                            placeable.placeRelative(0, 0)
                                        }
                                    }) {
                                cellContent(columnIndex, rowIndex)
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        fontWeight = if (isHeader) FontWeight.Bold else null,
        modifier = Modifier
            .padding(8.dp)
    )
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
    onViewSelected: (View) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        YearSelector(
            years = years,
            onYearSelected = onYearSelected,
            modifier = Modifier.weight(0.5F)
        )
        ViewSelector(
            views = views,
            onViewSelected = onViewSelected,
            modifier = Modifier.weight(0.5F)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun YearSelector(years: List<Year>, onYearSelected: (Year) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = years.any { it.selected },
            onClick = { expanded = !expanded },
            label = { Text(years.first { it.selected }.year.toString()) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(id = R.string.report_year_selector_content_description),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    text = {
                        Text(year.year.toString())
                    },
                    onClick = {
                        expanded = false
                        onYearSelected(year)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewSelector(
    views: List<View>,
    onViewSelected: (View) -> Unit,
    modifier: Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        FilterChip(
            selected = views.any { it.selected },
            onClick = { expanded = !expanded },
            label = { Text(stringResource(id = views.first { it.selected }.type.string())) },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(id = R.string.report_view_selector_content_description),
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            views.forEach { view ->
                DropdownMenuItem(
                    text = {
                        Text(stringResource(id = view.type.string()))
                    },
                    onClick = {
                        expanded = false
                        onViewSelected(view)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    items: List<FilterItem>,
    onItemClicked: (item: FilterItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(modifier = modifier.fillMaxWidth()) {
        items.forEach { filterItem ->
            item {
                FilterChip(
                    selected = filterItem.selected,
                    onClick = { onItemClicked(filterItem) },
                    label = { Text(filterItem.displayName) }
                )
                Spacer(modifier = modifier.width(8.dp))
            }
        }
    }
}

@StringRes
fun ViewType.string(): Int {
    return when (this) {
        ViewType.TABLE -> R.string.report_view_table
        ViewType.BAR_MONTH -> R.string.report_view_bar_month
        ViewType.BAR_ACCOUNT -> R.string.report_view_bar_account
    }
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