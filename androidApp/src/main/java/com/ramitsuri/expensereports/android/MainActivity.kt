package com.ramitsuri.expensereports.android

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.DecimalMode
import com.ramitsuri.expensereports.android.extensions.shutdown
import com.ramitsuri.expensereports.data.AccountTotal
import com.ramitsuri.expensereports.ui.FilterItem
import com.ramitsuri.expensereports.utils.ExpenseReportView
import com.ramitsuri.expensereports.viewmodel.ExpenseReportViewModel
import com.ramitsuri.expensereports.viewmodel.View
import com.ramitsuri.expensereports.viewmodel.ViewType
import com.ramitsuri.expensereports.viewmodel.Year
import org.koin.androidx.viewmodel.ext.android.getViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                ExpenseReportScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ExpenseReportScreen(viewModel: ExpenseReportViewModel = getViewModel()) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Scaffold { paddingValues ->
                val viewState = viewModel.state.collectAsState().value
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .statusBarsPadding()
                        .displayCutoutPadding()
                        .padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (viewState.loading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    } else {
                        if (viewState.error != null) {
                            Toast.makeText(
                                this@MainActivity,
                                viewState.error.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            viewModel.onErrorShown()
                        }

                        TopRow(
                            years = viewState.years,
                            onYearSelected = viewModel::reportSelected,
                            views = viewState.views,
                            onViewSelected = viewModel::onViewSelected,
                            serverUrl = viewState.serverUrl,
                            onUrlSet = viewModel::setServerUrl,
                            onRefreshRequested = viewModel::refresh
                        )
                        FilterRow(
                            items = viewState.accounts,
                            onItemClicked = viewModel::onAccountClicked
                        )
                        FilterRow(
                            items = viewState.months,
                            onItemClicked = viewModel::onMonthClicked
                        )

                        val report = viewState.report
                        if (report == null) {
                            ReportUnavailable()
                        } else {
                            ReportView(report = report)
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ReportView(
        report: ExpenseReportView
    ) {
        when (report) {
            is ExpenseReportView.Full -> {
                TableView(
                    report.accountTotals,
                    report.total,
                    report.sortedMonths
                )
            }

            is ExpenseReportView.ByMonth -> {
                BarChartMonth(report.monthTotals, report.total)
            }

            is ExpenseReportView.ByAccount -> {
                BarChartAccount(report.accountTotals, report.total)
            }
            else -> {}
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
                LazyColumn {
                    accounts.forEach { (account, amount) ->
                        item {
                            val value = amount.divide(total, decimalMode = DecimalMode.US_CURRENCY)
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
                LazyColumn {
                    months.forEach { (month, amount) ->
                        item {
                            val value = amount.divide(total, decimalMode = DecimalMode.US_CURRENCY)
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

    @Composable
    private fun BarChartBar(value: Float, label1: String, label2: String) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(0.2f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(text = label1, style = MaterialTheme.typography.bodySmall)
                Text(text = label2, style = MaterialTheme.typography.bodySmall)
            }
            LinearProgressIndicator(
                progress = value,
                modifier = Modifier
                    .weight(0.7F)
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeCap = StrokeCap.Round
            )
        }
    }

    @Composable
    private fun TableView(
        accountTotals: List<AccountTotal>,
        total: AccountTotal,
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
                    if (rowIndex == 0) { // Headers
                        if (columnIndex == 0) {
                            TableCell(text = "Accounts", isHeader = true)
                        } else if (columnIndex == columns - 1) {
                            TableCell(text = "Total", isHeader = true)
                        } else {
                            TableCell(
                                text = stringResource(id = sortedMonths[columnIndex - 1].string()),
                                isHeader = true
                            )
                        }
                    } else if (rowIndex == 1) { // Totals Account row
                        if (columnIndex == 0) {
                            TableCell(text = "Total", isHeader = true)
                        } else if (columnIndex == columns - 1) {
                            TableCell(text = total.total.toStringExpanded(), isHeader = true)
                        } else {
                            val month = sortedMonths[columnIndex - 1]
                            TableCell(
                                text = total.monthAmounts[month]?.toStringExpanded() ?: "0.0",
                                isHeader = true
                            )
                        }
                    } else { // Other Accounts
                        val account = accountTotals[rowIndex - 2]
                        if (columnIndex == 0) {
                            TableCell(text = account.name, isHeader = true)
                        } else if (columnIndex == columns - 1) {
                            TableCell(
                                text = account.total.toStringExpanded(),
                                isHeader = true
                            )
                        } else {
                            val month = sortedMonths[columnIndex - 1]
                            TableCell(
                                text = account.monthAmounts[month]?.toStringExpanded() ?: "0.0",
                                isHeader = false
                            )
                        }
                    }
                })
        }
    }

    @Composable
    fun Table(
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
                                                MaterialTheme.colorScheme.onPrimaryContainer
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
    fun TableCell(
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
    fun ReportUnavailable() {

    }

    @Composable
    fun TopRow(
        years: List<Year>,
        onYearSelected: (Year) -> Unit,
        views: List<View>,
        onViewSelected: (View) -> Unit,
        serverUrl: String,
        onUrlSet: (String) -> Unit,
        onRefreshRequested: () -> Unit
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
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
            MoreMenu(
                serverUrl = serverUrl,
                onUrlSet = onUrlSet,
                onRefreshRequested = onRefreshRequested
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun YearSelector(years: List<Year>, onYearSelected: (Year) -> Unit, modifier: Modifier) {
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
    fun ViewSelector(
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
    fun FilterRow(
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

    @Composable
    fun MoreMenu(
        serverUrl: String,
        onUrlSet: (String) -> Unit,
        onRefreshRequested: () -> Unit
    ) {
        val dialogState = rememberSaveable { mutableStateOf(false) }
        var expanded by remember { mutableStateOf(false) }
        var serverSet by rememberSaveable { mutableStateOf(false) }

        if (dialogState.value) {
            SetApiUrlDialog(
                dialogState = dialogState,
                previousUrl = serverUrl,
                onPositiveClick = { value ->
                    dialogState.value = !dialogState.value
                    onUrlSet(value)
                    serverSet = true
                },
                onNegativeClick = {
                    dialogState.value = !dialogState.value
                }
            )
        }
        val context = LocalContext.current


        Box {
            IconButton(
                onClick = {
                    expanded = !expanded
                },
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.MoreVert,
                    contentDescription = stringResource(id = R.string.menu_content_description)
                )
            }
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {

                DropdownMenuItem(
                    text = { Text(stringResource(id = HomeMenuItem.REFRESH.textResId)) },
                    onClick = {
                        expanded = false
                        onRefreshRequested()
                    }
                )
                if (serverSet) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = HomeMenuItem.RESTART.textResId)) },
                        onClick = {
                            expanded = false
                            context.shutdown()
                        }
                    )
                } else if (serverUrl.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = HomeMenuItem.SET_SERVER.textResId)) },
                        onClick = {
                            expanded = false
                            dialogState.value = true
                        }
                    )
                } else {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = HomeMenuItem.SERVER_SET.textResId)) },
                        onClick = {
                            expanded = false
                            dialogState.value = true
                        }
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SetApiUrlDialog(
        previousUrl: String,
        onPositiveClick: (String) -> Unit,
        onNegativeClick: () -> Unit,
        modifier: Modifier = Modifier,
        dialogState: MutableState<Boolean>
    ) {
        var text by rememberSaveable { mutableStateOf(previousUrl.ifEmpty { "http://" }) }
        Dialog(
            onDismissRequest = { dialogState.value = false },
            properties = DialogProperties(dismissOnClickOutside = true)
        ) {
            Card {
                Column(modifier = modifier.padding(8.dp)) {
                    OutlinedTextField(
                        value = text,
                        singleLine = true,
                        onValueChange = { text = it },
                        modifier = modifier.fillMaxWidth()
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = modifier.fillMaxWidth()
                    ) {
                        TextButton(onClick = {
                            onNegativeClick()
                        }) {
                            Text(text = stringResource(id = R.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        TextButton(onClick = {
                            onPositiveClick(text)
                        }) {
                            Text(text = stringResource(id = R.string.ok))
                        }
                    }
                }
            }
        }
    }
}

enum class HomeMenuItem(val id: Int, @StringRes val textResId: Int) {
    REFRESH(1, R.string.home_menu_refresh),
    SET_SERVER(2, R.string.home_menu_set_server),
    SERVER_SET(3, R.string.home_menu_server_set),
    RESTART(4, R.string.home_menu_restart),
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