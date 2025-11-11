package com.ramitsuri.expensereports.ui.report

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ramitsuri.expensereports.model.MonthYear
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.model.formatted
import com.ramitsuri.expensereports.ui.components.ReportTable
import com.ramitsuri.expensereports.utils.formatted
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.cancel
import expensereports.shared.generated.resources.ok
import expensereports.shared.generated.resources.report_error_body
import expensereports.shared.generated.resources.report_error_title
import expensereports.shared.generated.resources.year
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    viewState: ReportViewState,
    onReportSelected: (ReportNames) -> Unit,
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .displayCutoutPadding(),
    ) {
        val scrollBehavior =
            TopAppBarDefaults.enterAlwaysScrollBehavior(
                rememberTopAppBarState(),
            )
        Toolbar(
            scrollBehavior = scrollBehavior,
            selectedReport = viewState.selectedReport,
            selectedPeriod = viewState.selectedPeriod,
            onReportSelected = onReportSelected,
            onPeriodSelected = onPeriodSelected,
            onBack = onBack,
        )
        if (viewState.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        } else if (viewState.report == null) {
            AlertDialog(
                onDismissRequest = onBack,
                title = {
                    Text(text = stringResource(Res.string.report_error_title))
                },
                text = {
                    Text(text = stringResource(Res.string.report_error_body))
                },
                confirmButton = {
                    TextButton(onClick = onBack) {
                        Text(stringResource(Res.string.ok))
                    }
                },
                dismissButton = { },
            )
        } else {
            ReportTable(viewState.report)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    selectedReport: ReportNames,
    selectedPeriod: Period,
    scrollBehavior: TopAppBarScrollBehavior,
    onReportSelected: (ReportNames) -> Unit,
    onPeriodSelected: (Period) -> Unit,
    onBack: () -> Unit,
) {
    TopAppBar(
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = {
            Row {
                ReportSelector(
                    selectedReport = selectedReport,
                    onReportSelected = onReportSelected,
                )
                Spacer(modifier = Modifier.weight(1f))
                PeriodSelector(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = onPeriodSelected,
                )
            }
        },
        navigationIcon = {
            IconButton(
                onClick = onBack,
                modifier =
                    Modifier
                        .size(48.dp)
                        .padding(4.dp),
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "",
                )
            }
        },
        actions = { },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ReportSelector(
    selectedReport: ReportNames,
    onReportSelected: (ReportNames) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clickable(onClick = { showMenu = true })
                    .padding(8.dp),
        ) {
            Text(text = selectedReport.name, style = MaterialTheme.typography.bodySmall)
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "dropdown",
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            ReportNames.all.forEach {
                DropdownMenuItem(
                    text = { Text(it.name, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        showMenu = false
                        onReportSelected(it)
                    },
                )
            }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedPeriod: Period,
    onPeriodSelected: (Period) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showCustomPeriodDialog by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clickable(onClick = { showMenu = true })
                    .padding(8.dp),
        ) {
            Text(text = selectedPeriod.formatted(), style = MaterialTheme.typography.bodySmall)
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "dropdown",
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            Period.all.forEach {
                DropdownMenuItem(
                    text = { Text(it.formatted(), style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        showMenu = false
                        if (it is Period.Custom) {
                            showCustomPeriodDialog = true
                        } else {
                            onPeriodSelected(it)
                        }
                    },
                )
            }
        }
    }
    CustomPeriodDialog(
        show = showCustomPeriodDialog,
        onPeriodSet = {
            onPeriodSelected(it)
            showCustomPeriodDialog = false
        },
        onDismiss = { showCustomPeriodDialog = false },
    )
}

@Composable
private fun CustomPeriodDialog(
    show: Boolean,
    onPeriodSet: (Period.Custom) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var startMonth: Month by remember { mutableStateOf(Month.JANUARY) }
    var startYear: Int? by remember { mutableStateOf(null) }
    var endMonth: Month by remember { mutableStateOf(Month.DECEMBER) }
    var endYear: Int? by remember { mutableStateOf(null) }

    if (show) {
        Dialog(onDismissRequest = { }) {
            Card {
                Column(modifier = modifier.padding(16.dp)) {
                    MonthYearRow(
                        selectedMonth = startMonth,
                        selectedYear = startYear,
                        onYearSet = { startYear = it },
                        onMonthSet = { startMonth = it },
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    MonthYearRow(
                        selectedMonth = endMonth,
                        selectedYear = endYear,
                        onYearSet = { endYear = it },
                        onMonthSet = { endMonth = it },
                    )
                    Spacer(modifier = modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = modifier.fillMaxWidth(),
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(Res.string.cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(
                            onClick = {
                                val startMonthYear = startYear?.let { MonthYear(startMonth, it) }
                                val endMonthYear = endYear?.let { MonthYear(endMonth, it) }
                                if (startMonthYear == null || endMonthYear == null) return@TextButton
                                onPeriodSet(
                                    Period.Custom(
                                        start = startMonthYear,
                                        end = endMonthYear,
                                    ),
                                )
                            },
                        ) {
                            Text(text = stringResource(Res.string.ok))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthYearRow(
    selectedMonth: Month,
    selectedYear: Int?,
    onYearSet: (Int) -> Unit,
    onMonthSet: (Month) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MonthSelector(selectedMonth = selectedMonth, onMonthSet = onMonthSet)
        OutlinedTextField(
            value = selectedYear?.toString() ?: "",
            singleLine = true,
            label = {
                Text(stringResource(Res.string.year))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            onValueChange = { onYearSet(it.take(4).toInt()) },
        )
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: Month,
    onMonthSet: (Month) -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
                Modifier
                    .clickable(onClick = { showMenu = true })
                    .padding(8.dp),
        ) {
            Text(text = selectedMonth.formatted(), style = MaterialTheme.typography.bodyMedium)
            Icon(
                imageVector = Icons.Outlined.ArrowDropDown,
                contentDescription = "dropdown",
            )
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
        ) {
            Month.entries.forEach {
                DropdownMenuItem(
                    text = { Text(it.formatted(), style = MaterialTheme.typography.bodyMedium) },
                    onClick = {
                        showMenu = false
                        onMonthSet(it)
                    },
                )
            }
        }
    }
}
