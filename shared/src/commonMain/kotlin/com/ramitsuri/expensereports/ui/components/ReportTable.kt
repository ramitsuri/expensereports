package com.ramitsuri.expensereports.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.model.ReportForTable
import com.seanproctor.datatable.DataColumn
import com.seanproctor.datatable.DataTableState
import com.seanproctor.datatable.material3.DataTable
import com.seanproctor.datatable.rememberDataTableState

@Composable
fun ReportTable(report: ReportForTable) {
    val headerColumnState = rememberDataTableState()
    val totalsState = rememberDataTableState()
    LaunchedEffect(report) {
        headerColumnState.verticalScrollState.scrollTo(0)
        headerColumnState.horizontalScrollState.scrollTo(0)
        totalsState.verticalScrollState.scrollTo(0)
        totalsState.horizontalScrollState.scrollTo(0)
    }

    LaunchedEffect(headerColumnState.verticalScrollState.offset) {
        if (!totalsState.verticalScrollState.isScrollInProgress) {
            totalsState.verticalScrollState.scrollTo(headerColumnState.verticalScrollState.offset)
        }
    }

    LaunchedEffect(totalsState.verticalScrollState.offset) {
        if (!headerColumnState.verticalScrollState.isScrollInProgress) {
            headerColumnState.verticalScrollState.scrollTo(totalsState.verticalScrollState.offset)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            HeaderColumn(headerColumnState, report.headerColumn)
            Totals(totalsState, report.headerRow, report.rows)
        }
        Spacer(modifier = Modifier.height(64.dp))
    }
}

@Composable
private fun HeaderColumn(
    state: DataTableState,
    accountNames: List<String>,
) {
    DataTable(
        state = state,
        columns =
            listOf(
                DataColumn {
                    Text("", style = MaterialTheme.typography.bodySmall)
                },
            ),
    ) {
        accountNames.forEach {
            row {
                cell {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun Totals(
    state: DataTableState,
    headerRow: List<String>,
    rows: List<List<String>>,
) {
    DataTable(
        state = state,
        columns =
            headerRow.map {
                DataColumn {
                    Text(it)
                }
            },
    ) {
        rows.forEach { row ->
            row {
                row.forEach {
                    cell { Text(it) }
                }
            }
        }
    }
}
