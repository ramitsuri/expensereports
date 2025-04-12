package com.ramitsuri.expensereports.ui.report

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.model.Period
import com.ramitsuri.expensereports.model.ReportNames
import com.ramitsuri.expensereports.model.formatted
import com.ramitsuri.expensereports.ui.components.ReportTable
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.ok
import expensereports.shared.generated.resources.report_error_body
import expensereports.shared.generated.resources.report_error_title
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
                        onPeriodSelected(it)
                    },
                )
            }
        }
    }
}
