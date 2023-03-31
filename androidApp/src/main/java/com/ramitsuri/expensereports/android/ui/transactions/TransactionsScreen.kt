package com.ramitsuri.expensereports.android.ui.transactions

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.ui.views.Table
import com.ramitsuri.expensereports.android.ui.views.TableCell
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.monthDateYear
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.viewmodel.TransactionsFilter
import com.ramitsuri.expensereports.viewmodel.TransactionsViewModel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import org.koin.androidx.compose.getViewModel
import java.time.format.DateTimeFormatter

@Composable
fun TransactionsScreen(
    modifier: Modifier = Modifier,
    viewModel: TransactionsViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value
    TransactionsContent(
        isLoading = viewState.loading,
        transactions = viewState.transactions,
        filter = viewState.filter,
        onFilterUpdated = viewModel::onFilterUpdated,
        modifier = modifier,
    )
}

@Composable
fun TransactionsContent(
    isLoading: Boolean,
    transactions: List<Transaction>,
    filter: TransactionsFilter,
    onFilterUpdated: (startDate: LocalDate?, endDate: LocalDate?, minAmount: BigDecimal?, maxAmount: BigDecimal?) -> Unit,
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
            FilterRow(
                filter = filter,
                onFilterUpdated = onFilterUpdated
            )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    filter: TransactionsFilter,
    onFilterUpdated: (startDate: LocalDate?, endDate: LocalDate?, minAmount: BigDecimal?, maxAmount: BigDecimal?) -> Unit,
    modifier: Modifier = Modifier
) {
    val filterDialogState = rememberSaveable { mutableStateOf(false) }

    val startDate: MutableState<String> =
        rememberSaveable { mutableStateOf(filter.startDate.string()) }
    val endDate: MutableState<String> = rememberSaveable { mutableStateOf(filter.endDate.string()) }
    val minAmount: MutableState<String> =
        rememberSaveable { mutableStateOf(filter.minAmount.string()) }
    val maxAmount: MutableState<String> =
        rememberSaveable { mutableStateOf(filter.maxAmount.string()) }

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { // Date filter
                val startDateValue = startDate.value.toLocalDate() ?: filter.startDate
                val endDateValue = endDate.value.toLocalDate() ?: filter.endDate
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.transactions_filter_date_range),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = startDateValue != null && endDateValue != null,
                    onClick = {
                        filterDialogState.value = true
                    },
                    label = {
                        Text(
                            text = if (startDateValue != null && endDateValue != null) {
                                "${startDateValue.monthDateYear()} - ${endDateValue.monthDateYear()}"
                            } else {
                                stringResource(id = R.string.transactions_filter_date_range)
                            }
                        )
                    }
                )
            }
            item { // Amount filter
                val minAmountValue = minAmount.value.toBigDecimal() ?: filter.minAmount
                val maxAmountValue = maxAmount.value.toBigDecimal() ?: filter.maxAmount
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.transactions_filter_amount_range),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = minAmountValue != null && maxAmountValue != null,
                    onClick = {
                        filterDialogState.value = true
                    },
                    label = {
                        Text(
                            text = if (minAmountValue != null && maxAmountValue != null) {
                                "${minAmountValue.format()} - ${maxAmountValue.format()}"
                            } else {
                                stringResource(id = R.string.transactions_filter_amount_range)
                            }
                        )
                    }
                )
            }
        }
    }
    if (filterDialogState.value) {
        FilterDialog(
            startDate = startDate.value,
            startDateUpdated = {
                startDate.value = it
            },
            endDate = endDate.value,
            endDateUpdated = {
                endDate.value = it
            },
            minAmount = minAmount.value,
            minAmountUpdated = {
                minAmount.value = it
            },
            maxAmount = maxAmount.value,
            maxAmountUpdated = {
                maxAmount.value = it
            },
            onPositiveClick = {
                onFilterUpdated(
                    startDate.value.toLocalDate(),
                    endDate.value.toLocalDate(),
                    minAmount.value.toBigDecimal(),
                    maxAmount.value.toBigDecimal(),
                )
                filterDialogState.value = false
            },
            onNegativeClick = {
                startDate.value = filter.startDate.string()
                endDate.value = filter.endDate.string()
                minAmount.value = filter.minAmount.string()
                maxAmount.value = filter.maxAmount.string()
                filterDialogState.value = false
            },
            dialogState = filterDialogState
        )
    }
}

@Composable
private fun FilterDialog(
    startDate: String,
    startDateUpdated: (String) -> Unit,
    endDate: String,
    endDateUpdated: (String) -> Unit,
    minAmount: String,
    minAmountUpdated: (String) -> Unit,
    maxAmount: String,
    maxAmountUpdated: (String) -> Unit,
    onPositiveClick: () -> Unit,
    onNegativeClick: () -> Unit,
    modifier: Modifier = Modifier,
    dialogState: MutableState<Boolean>
) {
    Dialog(
        onDismissRequest = { dialogState.value = false },
        properties = DialogProperties(dismissOnClickOutside = true)
    ) {
        Card {
            Column(modifier = modifier.padding(16.dp)) {
                FilterField(
                    text = startDate,
                    labelRes = R.string.transactions_filter_start_date_label,
                    placeholderRes = R.string.transactions_filter_date_format_hint,
                    textUpdated = startDateUpdated,
                    modifier = modifier.fillMaxWidth()
                )
                Spacer(modifier = modifier.height(8.dp))
                FilterField(
                    text = endDate,
                    labelRes = R.string.transactions_filter_end_date_label,
                    placeholderRes = R.string.transactions_filter_date_format_hint,
                    textUpdated = endDateUpdated,
                    modifier = modifier.fillMaxWidth()
                )
                Spacer(modifier = modifier.height(16.dp))
                FilterField(
                    text = minAmount,
                    labelRes = R.string.transactions_filter_min_amount_label,
                    placeholderRes = R.string.transactions_filter_amount_format_hint,
                    textUpdated = minAmountUpdated,
                    modifier = modifier.fillMaxWidth()
                )
                Spacer(modifier = modifier.height(8.dp))
                FilterField(
                    text = maxAmount,
                    labelRes = R.string.transactions_filter_max_amount_label,
                    placeholderRes = R.string.transactions_filter_amount_format_hint,
                    textUpdated = maxAmountUpdated,
                    modifier = modifier.fillMaxWidth()
                )
                Spacer(modifier = modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = onNegativeClick) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = onPositiveClick) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterField(
    text: String,
    @StringRes labelRes: Int,
    @StringRes placeholderRes: Int,
    textUpdated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = text,
        label = {
            Text(text = stringResource(id = labelRes))
        },
        placeholder = {
            Text(text = stringResource(id = placeholderRes))
        },
        trailingIcon = {
            if (text.isNotEmpty()) {
                IconButton(onClick = { textUpdated("") }) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                }
            }
        },
        singleLine = true,
        onValueChange = { textUpdated(it) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number
        ),
        modifier = modifier.fillMaxWidth()
    )
}

private fun String.toLocalDate(): LocalDate? {
    return try {
        java.time.LocalDate.parse(this, DateTimeFormatter.ofPattern("MM-dd-uuuu"))
            .toKotlinLocalDate()
    } catch (e: Exception) {
        println(e.message)
        null
    }
}

private fun LocalDate?.string(): String {
    return this?.toJavaLocalDate()?.format(DateTimeFormatter.ofPattern("MM-dd-uuuu")) ?: ""
}

private fun String.toBigDecimal(): BigDecimal? {
    if (this.isEmpty()) return null
    return try {
        BigDecimal.parseString(this)
    } catch (e: Exception) {
        println(e.message)
        null
    }
}

private fun BigDecimal?.string(): String {
    return this?.toStringExpanded() ?: ""
}