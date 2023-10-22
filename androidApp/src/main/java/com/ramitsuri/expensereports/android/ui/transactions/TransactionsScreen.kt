package com.ramitsuri.expensereports.android.ui.transactions

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ionspin.kotlin.bignum.decimal.toBigDecimal
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.ui.views.AccountsFilterDialog
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.monthDateYear
import com.ramitsuri.expensereports.data.Split
import com.ramitsuri.expensereports.data.Transaction
import com.ramitsuri.expensereports.ui.Account
import com.ramitsuri.expensereports.viewmodel.TransactionsFilter
import com.ramitsuri.expensereports.viewmodel.TransactionsViewModel
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toKotlinLocalDate
import kotlinx.datetime.toLocalDateTime
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
    filter: TransactionsFilter?,
    onFilterUpdated: (startDate: LocalDate, endDate: LocalDate, minAmount: BigDecimal, maxAmount: BigDecimal, fromAccounts: List<Account>, toAccounts: List<Account>, searchTerm: String) -> Unit,
    modifier: Modifier = Modifier,
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
            Transactions(transactions = transactions)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun Transactions(transactions: List<Transaction>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        val transactionsGrouped = transactions.groupBy { it.date }
        transactionsGrouped.forEach { (date, transactions) ->
            stickyHeader {
                TransactionItemHeader(date = date)
            }
            items(transactions) { item ->
                TransactionItem(item)
            }
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

val AccountSaver = run {
    val nameKey = "Name"
    val fullNameKey = "FullName"
    val levelKey = "Level"
    val selectedKey = "Selected"
    mapSaver<Account>(
        save = {
            mapOf(
                nameKey to it.name,
                fullNameKey to it.fullName,
                levelKey to it.level,
                selectedKey to it.selected
            )
        },
        restore = {
            Account(
                name = it[nameKey] as String,
                fullName = it[fullNameKey] as String,
                level = it[levelKey] as Int,
                selected = it[selectedKey] as Boolean
            )
        }
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(
    filter: TransactionsFilter?,
    onFilterUpdated: (startDate: LocalDate, endDate: LocalDate, minAmount: BigDecimal, maxAmount: BigDecimal, fromAccounts: List<Account>, toAccounts: List<Account>, searchTerm: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val startDateFilterDialogState = rememberSaveable { mutableStateOf(false) }
    val endDateFilterDialogState = rememberSaveable { mutableStateOf(false) }
    val amountFilterDialogState = rememberSaveable { mutableStateOf(false) }
    val fromAccountsDialogState = rememberSaveable { mutableStateOf(false) }
    val toAccountsDialogState = rememberSaveable { mutableStateOf(false) }
    val searchTermDialogState = rememberSaveable { mutableStateOf(false) }

    val startDate: MutableState<String> =
        rememberSaveable { mutableStateOf(filter?.startDate.string()) }
    val startDateValue = startDate.value.localDate() ?: filter?.startDate

    val endDate: MutableState<String> =
        rememberSaveable { mutableStateOf(filter?.endDate.string()) }
    val endDateValue = endDate.value.localDate() ?: filter?.endDate

    val minAmount: MutableState<String> =
        rememberSaveable { mutableStateOf(filter?.minAmount.string()) }
    val minAmountValue = minAmount.value.bigDecimal() ?: filter?.minAmount

    val maxAmount: MutableState<String> =
        rememberSaveable { mutableStateOf(filter?.maxAmount.string()) }
    val maxAmountValue = maxAmount.value.bigDecimal() ?: filter?.maxAmount

    val fromAccounts = remember { mutableStateOf(filter?.fromAccounts ?: listOf()) }
    val toAccounts = remember { mutableStateOf(filter?.toAccounts ?: listOf()) }

    val searchTerm = remember { mutableStateOf("") }
    val searchTermValue = searchTerm.value

    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item { // Description search
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(id = R.string.transactions_filter_description_label),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = true,
                    onClick = {
                        searchTermDialogState.value = true
                    },
                    label = {
                        Text(
                            text = stringResource(id = R.string.transactions_filter_description_label)
                        )
                    }
                )
            }
            item { // Start date
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.transactions_filter_start_date_label),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = startDateValue != null,
                    onClick = {
                        startDateFilterDialogState.value = true
                    },
                    label = {
                        Text(
                            text = startDateValue?.monthDateYear()
                                ?: stringResource(id = R.string.transactions_filter_start_date_label)
                        )
                    }
                )
            }
            item { // End date
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.transactions_filter_end_date_label),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = endDateValue != null,
                    onClick = {
                        endDateFilterDialogState.value = true
                    },
                    label = {
                        Text(
                            text = endDateValue?.monthDateYear()
                                ?: stringResource(id = R.string.transactions_filter_end_date_label)
                        )
                    }
                )
            }
            item { // Amount filter
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
                        amountFilterDialogState.value = true
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
            item { // From accounts filter
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.transactions_filter_amount_range),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = true,
                    onClick = {
                        fromAccountsDialogState.value = true
                    },
                    label = {
                        Text(
                            text = "From accounts"
                        )
                    }
                )
            }
            item { // To accounts filter
                FilterChip(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.KeyboardArrowDown,
                            contentDescription = stringResource(id = R.string.transactions_filter_amount_range),
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    },
                    selected = true,
                    onClick = {
                        toAccountsDialogState.value = true
                    },
                    label = {
                        Text(
                            text = "To accounts"
                        )
                    }
                )
            }
        }
    }
    fun updateFilter() {
        onFilterUpdated(
            startDate.value.localDate()!!,
            endDate.value.localDate()!!,
            minAmount.value.bigDecimal()!!,
            maxAmount.value.bigDecimal()!!,
            fromAccounts.value,
            toAccounts.value,
            searchTerm.value,
        )
    }
    if (searchTermDialogState.value) {
        SearchTermFilterDialog(
            searchTerm = searchTermValue,
            onSearchTermUpdated = {
                searchTerm.value = it
                updateFilter()
            },
            dialogState = searchTermDialogState
        )
    }
    if (startDateFilterDialogState.value) {
        DateFilterDialog(
            title = stringResource(id = R.string.transactions_filter_start_date_label),
            initialDate = startDateValue,
            onDateUpdated = {
                startDate.value = it.string()
                updateFilter()
            },
            dialogState = startDateFilterDialogState
        )
    }
    if (endDateFilterDialogState.value) {
        DateFilterDialog(
            title = stringResource(id = R.string.transactions_filter_end_date_label),
            initialDate = endDateValue,
            onDateUpdated = {
                endDate.value = it.string()
                updateFilter()
            },
            dialogState = endDateFilterDialogState
        )
    }
    if (amountFilterDialogState.value) {
        AmountRangeFilterDialog(
            minAmount = minAmountValue,
            maxAmount = maxAmountValue,
            onAmountRangeUpdated = { selectedMinAmount, selectedMaxAmount ->
                minAmount.value = selectedMinAmount.string()
                maxAmount.value = selectedMaxAmount.string()
                updateFilter()
            },
            dialogState = amountFilterDialogState
        )
    }
    if (fromAccountsDialogState.value) {
        AccountsFilterDialog(
            accounts = fromAccounts.value,
            onAccountFiltersApplied = { accounts ->
                fromAccounts.value = accounts
                updateFilter()
            },
            dialogState = fromAccountsDialogState
        )
    }
    if (toAccountsDialogState.value) {
        AccountsFilterDialog(
            accounts = toAccounts.value,
            onAccountFiltersApplied = { accounts ->
                toAccounts.value = accounts
                updateFilter()
            },
            dialogState = toAccountsDialogState
        )
    }
}

@Composable
private fun SearchTermFilterDialog(
    searchTerm: String,
    onSearchTermUpdated: (String) -> Unit,
    dialogState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    var enteredSearchTerm: String by remember { mutableStateOf(searchTerm) }
    Dialog(
        onDismissRequest = { dialogState.value = false },
        properties = DialogProperties(dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Card(modifier = modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val title = stringResource(id = R.string.transactions_filter_amount_range)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                FilterField(
                    text = enteredSearchTerm,
                    labelRes = R.string.transactions_filter_description_label,
                    placeholderRes = R.string.transactions_filter_description_hint,
                    textUpdated = { enteredSearchTerm = it },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Text,
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        dialogState.value = false
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        dialogState.value = false
                        onSearchTermUpdated(enteredSearchTerm)
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateFilterDialog(
    title: String,
    initialDate: LocalDate?,
    onDateUpdated: (LocalDate) -> Unit,
    dialogState: MutableState<Boolean>
) {
    val initialSelectedDateMillis = initialDate
        ?.atTime(LocalTime(hour = 0, minute = 0, second = 0))
        ?.toInstant(TimeZone.UTC)
        ?.toEpochMilliseconds()
    val datePickerState =
        rememberDatePickerState(initialSelectedDateMillis = initialSelectedDateMillis)
    DatePickerDialog(
        onDismissRequest = {
            dialogState.value = false
        },
        confirmButton = {
            TextButton(onClick = {
                dialogState.value = false
                val selectedDateMillis = datePickerState.selectedDateMillis ?: return@TextButton
                val selectedDate = Instant.fromEpochMilliseconds(selectedDateMillis)
                    .toLocalDateTime(TimeZone.UTC)
                    .date
                onDateUpdated(selectedDate)
            }) {
                Text(text = stringResource(id = R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                dialogState.value = false
            }) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    ) {
        DatePicker(state = datePickerState, title = {
            Text(
                text = title,
                modifier = Modifier.padding(PaddingValues(start = 24.dp, end = 12.dp, top = 16.dp))
            )
        })
    }
}

@Composable
private fun AmountRangeFilterDialog(
    minAmount: BigDecimal?,
    maxAmount: BigDecimal?,
    onAmountRangeUpdated: (BigDecimal?, BigDecimal?) -> Unit,
    dialogState: MutableState<Boolean>,
    modifier: Modifier = Modifier
) {
    var showRangeSlider by remember { mutableStateOf(true) }
    var selectedMinValue: BigDecimal? by remember { mutableStateOf(minAmount) }
    var selectedMaxValue: BigDecimal? by remember { mutableStateOf(maxAmount) }

    Dialog(
        onDismissRequest = { dialogState.value = false },
        properties = DialogProperties(dismissOnClickOutside = true, usePlatformDefaultWidth = false)
    ) {
        Card(modifier = modifier.padding(16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                val title = stringResource(id = R.string.transactions_filter_amount_range)
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (showRangeSlider) {
                    val allowedRange = 0f..15_000f
                    val selectedMin = selectedMinValue?.floatValue(exactRequired = false)
                        ?: allowedRange.start
                    val selectedMax = selectedMaxValue?.floatValue(exactRequired = false)
                        ?: allowedRange.endInclusive

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${
                                selectedMin.toBigDecimal().format()
                            } - ${selectedMax.toBigDecimal().format()}"
                        )
                        IconButton(onClick = { showRangeSlider = false }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    RangeSlider(
                        modifier = Modifier.semantics {
                            contentDescription = title
                        },
                        value = selectedMin..selectedMax,
                        onValueChange = { range ->
                            selectedMinValue = range.start.toInt().toBigDecimal()
                            selectedMaxValue = range.endInclusive.toInt().toBigDecimal()
                        },
                        valueRange = allowedRange,
                        onValueChangeFinished = { },
                    )
                } else {
                    FilterField(
                        text = selectedMinValue.string(),
                        labelRes = R.string.transactions_filter_min_amount_label,
                        placeholderRes = R.string.transactions_filter_amount_format_hint,
                        textUpdated = { selectedMinValue = it.bigDecimal() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FilterField(
                        text = selectedMaxValue.string(),
                        labelRes = R.string.transactions_filter_max_amount_label,
                        placeholderRes = R.string.transactions_filter_amount_format_hint,
                        textUpdated = { selectedMaxValue = it.bigDecimal() },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        ),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(onClick = {
                        dialogState.value = false
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(onClick = {
                        dialogState.value = false
                        onAmountRangeUpdated(selectedMinValue, selectedMaxValue)
                    }) {
                        Text(text = stringResource(id = R.string.ok))
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterField(
    text: String,
    @StringRes labelRes: Int,
    @StringRes placeholderRes: Int,
    textUpdated: (String) -> Unit,
    keyboardOptions: KeyboardOptions,
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
        keyboardOptions = keyboardOptions,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun TransactionItemHeader(date: LocalDate, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = date.monthDateYear(),
            style = MaterialTheme.typography.titleMedium,
            modifier = modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .padding(8.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItem(
    transaction: Transaction
) {
    var showDetails by rememberSaveable { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = transaction.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    modifier = Modifier
                        .width(0.dp)
                        .weight(1f)
                        .padding(top = 4.dp)
                        .basicMarquee()
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = transaction.total.format(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .padding(top = 4.dp)
                )
            }
            IconButton(
                onClick = { showDetails = !showDetails },
                modifier = Modifier
                    .height(24.dp)
                    .fillMaxWidth()
            ) {
                val rotationAngle by animateFloatAsState(
                    targetValue = if (showDetails) 180F else 0F,
                    animationSpec = tween(durationMillis = 200, easing = FastOutLinearInEasing)

                )
                Icon(
                    imageVector =
                    Icons.Default.ExpandMore,
                    modifier = Modifier.rotate(rotationAngle),
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = showDetails) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val debitSplits = transaction.splits.filter { it.isDebit() }
                    val creditSplits = transaction.splits.filter { it.isCredit() }
                    Column(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(0.4f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        debitSplits.forEachIndexed { index, split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = split.account.split(":").last(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = split.amount.abs().format(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (index != debitSplits.lastIndex) {
                                Divider(
                                    modifier = Modifier
                                        .height(0.5.dp)
                                        .fillMaxWidth(0.5f),
                                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2F)
                                )
                            }
                        }
                    }
                    Icon(
                        painterResource(
                            id = R.drawable.ic_transfer
                        ),
                        contentDescription = null,
                        modifier = Modifier
                            .width(0.dp)
                            .weight(0.2f)
                    )
                    Column(
                        modifier = Modifier
                            .width(0.dp)
                            .weight(0.4f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        creditSplits.forEachIndexed { index, split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = split.account.split(":").last(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = split.amount.format(),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (index != creditSplits.lastIndex) {
                                Divider(
                                    modifier = Modifier
                                        .height(0.5.dp)
                                        .fillMaxWidth(0.5f),
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Preview
@Composable
fun TransactionItemPreview() {
    Surface {
        TransactionItem(
            Transaction(
                date = LocalDate.parse("2023-01-12"),
                description = "Google Cloud - Chores",
                total = BigDecimal.parseString("5"),
                splits = listOf(
                    Split(account = "Fidelity", amount = BigDecimal.parseString("-5")),
                    Split(account = "Groceries", amount = BigDecimal.parseString("2")),
                    Split(account = "Food", amount = BigDecimal.parseString("3")),
                ),
                num = ""
            )
        )
    }
}

private fun String.localDate(): LocalDate? {
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

private fun String.bigDecimal(): BigDecimal? {
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