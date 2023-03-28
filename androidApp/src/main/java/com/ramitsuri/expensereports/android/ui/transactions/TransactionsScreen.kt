package com.ramitsuri.expensereports.android.ui.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.ui.views.Table
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.monthDateYear
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.viewmodel.TransactionsViewModel
import org.koin.androidx.compose.getViewModel

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value
    TransactionsContent(
        isLoading = viewState.loading,
        transactions = viewState.transactions,
        modifier = modifier
    )
}

@Composable
fun TransactionsContent(
    isLoading: Boolean,
    transactions: List<Transaction>,
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
            TableView(transactions = transactions)
        }
    }
}

@Composable
private fun TableView(
    transactions: List<Transaction>
) {
    val rows = transactions.size + 1
    val columns = 5
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
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
                                    text = stringResource(id = R.string.transactions_date_header),
                                    isHeader = true
                                )
                            }
                            1 -> {
                                TableCell(
                                    text = stringResource(id = R.string.transactions_description_header),
                                    isHeader = true
                                )
                            }
                            2 -> {
                                TableCell(
                                    text = stringResource(id = R.string.transactions_amount_header),
                                    isHeader = true
                                )
                            }
                            3 -> {
                                TableCell(
                                    text = stringResource(id = R.string.transactions_from_accounts_header),
                                    isHeader = true
                                )
                            }
                            4 -> {
                                TableCell(
                                    text = stringResource(id = R.string.transactions_to_accounts_header),
                                    isHeader = true
                                )
                            }
                        }
                    }
                    else -> { // Transactions
                        val transaction = transactions[rowIndex - 1]
                        when (columnIndex) {
                            0 -> {
                                TableCell(
                                    text = transaction.date.monthDateYear()
                                )
                            }
                            1 -> {
                                TableCell(
                                    text = transaction.description
                                )
                            }
                            2 -> {
                                TableCell(
                                    text = transaction.amount.format()
                                )
                            }
                            3 -> {
                                TableCell(
                                    text = transaction.fromAccounts
                                        .joinToString {
                                            it.split(":").last()
                                        }
                                )
                            }
                            4 -> {
                                TableCell(
                                    text = transaction.toAccounts
                                        .joinToString {
                                            it.split(":").last()
                                        }
                                )
                            }
                        }
                    }
                }
            })
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TableCell(
    text: String,
    isHeader: Boolean = false
) {
    Text(
        text = text,
        fontWeight = if (isHeader) FontWeight.Bold else null,
        style = if (isHeader) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodySmall,
        maxLines = 1,
        modifier = Modifier
            .padding(8.dp)
            .sizeIn(maxWidth = 200.dp)
            .basicMarquee()
    )
}