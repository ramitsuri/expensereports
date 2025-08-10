package com.ramitsuri.expensereports.ui.transactions

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells.Fixed
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.model.Transaction
import com.ramitsuri.expensereports.model.TransactionSplit
import com.ramitsuri.expensereports.ui.components.Date
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.friendlyLocalDate
import expensereports.shared.generated.resources.Res
import expensereports.shared.generated.resources.transaction_description
import expensereports.shared.generated.resources.transactions_filter_amount_from_to
import expensereports.shared.generated.resources.transactions_filter_from
import expensereports.shared.generated.resources.transactions_filter_from_account
import expensereports.shared.generated.resources.transactions_filter_max_amount
import expensereports.shared.generated.resources.transactions_filter_min_amount
import expensereports.shared.generated.resources.transactions_filter_to
import expensereports.shared.generated.resources.transactions_filter_to_account
import io.ktor.client.request.invoke
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.stringResource
import java.math.BigDecimal

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewState: TransactionsViewState,
    windowSize: WindowSizeClass,
    onBack: () -> Unit,
    onFilterApplied: (
        description: String,
        startDate: LocalDate,
        endDate: LocalDate,
        fromAccount: String?,
        toAccount: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
    ) -> Unit,
    onFromAccountTextUpdated: (String) -> Unit,
    onToAccountTextUpdated: (String) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
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
            } else {
                Filter(
                    description = viewState.description,
                    selectedStartDate = viewState.startDate,
                    selectedEndDate = viewState.endDate,
                    filterFromAccount = viewState.filterFromAccount,
                    filterToAccount = viewState.filterToAccount,
                    filterMinAmount = viewState.filterMinAmount,
                    filterMaxAmount = viewState.filterMaxAmount,
                    fromAccountSuggestions = viewState.fromAccountSuggestions,
                    toAccountSuggestions = viewState.toAccountSuggestions,
                    onFilterApplied = onFilterApplied,
                    onFromAccountTextUpdated = onFromAccountTextUpdated,
                    onToAccountTextUpdated = onToAccountTextUpdated,
                )
                LazyVerticalGrid(
                    modifier =
                        Modifier
                            .nestedScroll(scrollBehavior.nestedScrollConnection)
                            .padding(horizontal = 16.dp),
                    columns =
                        if (windowSize.widthSizeClass == WindowWidthSizeClass.Compact) {
                            Fixed(1)
                        } else {
                            Fixed(2)
                        },
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    viewState.txOrDateList.forEach { txOrDate ->
                        when (txOrDate) {
                            is TransactionsViewState.TxOrDate.Date -> {
                                stickyHeader {
                                    Text(
                                        text = friendlyLocalDate(txOrDate.date),
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.background)
                                                .padding(8.dp),
                                    )
                                }
                            }

                            is TransactionsViewState.TxOrDate.Tx -> {
                                item {
                                    TransactionItem(
                                        transaction = txOrDate.tx,
                                        snackbarHostState = snackbarHostState,
                                    )
                                }
                            }
                        }
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Transaction,
    snackbarHostState: SnackbarHostState,
) {
    var isExpanded by remember { mutableStateOf(false) }
    var rotateCount by remember(Unit) { mutableIntStateOf(0) }
    val iconRotate by animateFloatAsState(
        targetValue = if (isExpanded) (0f + (rotateCount * 180f)) else (180f + ((rotateCount - 1) * 180f)),
        animationSpec = tween(300, easing = LinearOutSlowInEasing),
        label = "buttonRotate",
    )
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth(),
        ) {
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable {
                            isExpanded = !isExpanded
                            rotateCount++
                        }
                        .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Normal,
                    text = transaction.description,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    text = transaction.total.format(),
                    color = redColor,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    modifier =
                        Modifier.size(16.dp)
                            .rotate(iconRotate),
                )
            }
            AnimatedVisibility(isExpanded) {
                val fromSplits = remember(transaction) { transaction.fromSplits() }
                val toSplits = remember(transaction) { transaction.toSplits() }
                Column(modifier = Modifier.padding(vertical = 16.dp)) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Spacer(modifier = Modifier.width(8.dp))
                        fromSplits.forEach {
                            Split(it, snackbarHostState)
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "")
                        toSplits.forEach {
                            Split(it, snackbarHostState)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Composable
private fun Split(
    split: TransactionSplit,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier =
            Modifier
                .width(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        snackbarHostState
                            .showSnackbar(
                                message = split.accountName,
                                duration = SnackbarDuration.Short,
                            )
                    }
                }
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = split.accountName.split(":").last(),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = split.amount.format(), fontWeight = FontWeight.Bold)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Filter(
    description: String,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    filterFromAccount: String?,
    filterToAccount: String?,
    filterMinAmount: BigDecimal?,
    filterMaxAmount: BigDecimal?,
    fromAccountSuggestions: List<String>,
    toAccountSuggestions: List<String>,
    onFilterApplied: (
        description: String,
        startDate: LocalDate,
        endDate: LocalDate,
        fromAccount: String?,
        toAccount: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
    ) -> Unit,
    onFromAccountTextUpdated: (String) -> Unit,
    onToAccountTextUpdated: (String) -> Unit,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    AnimatedContent(filterExpanded, label = "FilterToggle") { expanded ->
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clickable(onClick = { if (!expanded) filterExpanded = true }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (expanded) {
                ExpandedFilterContent(
                    description = description,
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
                    fromAccount = filterFromAccount,
                    toAccount = filterToAccount,
                    minAmount = filterMinAmount,
                    maxAmount = filterMaxAmount,
                    fromAccountSuggestions = fromAccountSuggestions,
                    toAccountSuggestions = toAccountSuggestions,
                    onFilterApplied = { desc, start, end, from, to, min, max ->
                        onFilterApplied(desc, start, end, from, to, min, max)
                        filterExpanded = false
                    },
                    onFilterCanceled = {
                        filterExpanded = false
                    },
                    onFromAccountTextUpdated = onFromAccountTextUpdated,
                    onToAccountTextUpdated = onToAccountTextUpdated,
                )
            } else {
                CollapsedFilterContent(
                    description = description,
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
                    fromAccount = filterFromAccount,
                    toAccount = filterToAccount,
                    minAmount = filterMinAmount,
                    maxAmount = filterMaxAmount,
                    onFilterExpanded = {
                        filterExpanded = true
                    },
                )
            }
        }
    }
}

@Composable
private fun RowScope.CollapsedFilterContent(
    description: String,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    fromAccount: String?,
    toAccount: String?,
    minAmount: BigDecimal?,
    maxAmount: BigDecimal?,
    onFilterExpanded: () -> Unit,
) {
    Column(modifier = Modifier.weight(1f)) {
        if (description.isNotEmpty()) {
            Text(description)
        }
        if (!fromAccount.isNullOrEmpty()) {
            Text(stringResource(Res.string.transactions_filter_from, fromAccount))
        }
        if (!toAccount.isNullOrEmpty()) {
            Text(stringResource(Res.string.transactions_filter_to, toAccount))
        }
        if (minAmount != null || maxAmount != null) {
            val min = minAmount?.toPlainString() ?: "-"
            val max = maxAmount?.toPlainString() ?: "-"
            Text(stringResource(Res.string.transactions_filter_amount_from_to, min, max))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(friendlyLocalDate(selectedStartDate))
            Text(" - ")
            Text(friendlyLocalDate(selectedEndDate))
        }
    }
    IconButton(
        onClick = onFilterExpanded,
    ) {
        Icon(Icons.Outlined.ArrowDropDown, contentDescription = "Expand Filters")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RowScope.ExpandedFilterContent(
    description: String,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    fromAccount: String?,
    toAccount: String?,
    minAmount: BigDecimal?,
    maxAmount: BigDecimal?,
    fromAccountSuggestions: List<String>,
    toAccountSuggestions: List<String>,
    onFilterApplied: (
        description: String,
        startDate: LocalDate,
        endDate: LocalDate,
        fromAccount: String?,
        toAccount: String?,
        minAmount: BigDecimal?,
        maxAmount: BigDecimal?,
    ) -> Unit,
    onFilterCanceled: () -> Unit,
    onFromAccountTextUpdated: (String) -> Unit,
    onToAccountTextUpdated: (String) -> Unit,
) {
    var descriptionText by remember { mutableStateOf(TextFieldValue(description)) }
    var startDate by remember { mutableStateOf(selectedStartDate) }
    var endDate by remember { mutableStateOf(selectedEndDate) }

    var fromAccountTextSelection by remember(fromAccount) { mutableIntStateOf(fromAccount?.length ?: 0) }
    var toAccountTextSelection by remember(toAccount) { mutableIntStateOf(toAccount?.length ?: 0) }

    var minAmountText by remember { mutableStateOf(TextFieldValue(minAmount?.toPlainString() ?: "")) }
    var maxAmountText by remember { mutableStateOf(TextFieldValue(maxAmount?.toPlainString() ?: "")) }

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var fromAccountExpanded by remember { mutableStateOf(false) }
    var toAccountExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.weight(1f)) {
        OutlinedTextField(
            value = descriptionText,
            onValueChange = { descriptionText = it },
            label = { Text(stringResource(Res.string.transaction_description)) },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (descriptionText.text.isNotEmpty()) {
                    IconButton(onClick = { descriptionText = TextFieldValue("") }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear Description")
                    }
                }
            },
        )
        Spacer(modifier = Modifier.height(8.dp))

        // From Account with Autocomplete
        ExposedDropdownMenuBox(
            expanded = fromAccountExpanded,
            onExpandedChange = { fromAccountExpanded = !fromAccountExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = TextFieldValue(fromAccount ?: "", TextRange(fromAccountTextSelection)),
                onValueChange = {
                    onFromAccountTextUpdated(it.text)
                    fromAccountExpanded = it.text.isNotEmpty() && fromAccountSuggestions.isNotEmpty()
                },
                label = {
                    Text(stringResource(Res.string.transactions_filter_from_account))
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = fromAccountExpanded && fromAccountSuggestions.isNotEmpty(),
                onDismissRequest = { fromAccountExpanded = false },
            ) {
                fromAccountSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onFromAccountTextUpdated(suggestion)
                            fromAccountExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        // To Account with Autocomplete
        ExposedDropdownMenuBox(
            expanded = toAccountExpanded,
            onExpandedChange = { toAccountExpanded = !toAccountExpanded },
            modifier = Modifier.fillMaxWidth(),
        ) {
            OutlinedTextField(
                value = TextFieldValue(toAccount ?: "", TextRange(toAccountTextSelection)),
                onValueChange = {
                    onToAccountTextUpdated(it.text)
                    toAccountExpanded = it.text.isNotEmpty() && toAccountSuggestions.isNotEmpty()
                },
                label = {
                    Text(stringResource(Res.string.transactions_filter_to_account))
                },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryEditable),
                singleLine = true,
            )
            ExposedDropdownMenu(
                expanded = toAccountExpanded && toAccountSuggestions.isNotEmpty(),
                onDismissRequest = { toAccountExpanded = false },
            ) {
                toAccountSuggestions.forEach { suggestion ->
                    DropdownMenuItem(
                        text = { Text(suggestion) },
                        onClick = {
                            onToAccountTextUpdated(suggestion)
                            toAccountExpanded = false
                        },
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = minAmountText,
                onValueChange = { minAmountText = it },
                label = {
                    Text(stringResource(Res.string.transactions_filter_min_amount))
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (minAmountText.text.isNotEmpty()) {
                        IconButton(onClick = { minAmountText = TextFieldValue("") }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear Min Amount")
                        }
                    }
                },
            )
            OutlinedTextField(
                value = maxAmountText,
                onValueChange = { maxAmountText = it },
                label = {
                    Text(stringResource(Res.string.transactions_filter_max_amount))
                },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    if (maxAmountText.text.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                maxAmountText =
                                    TextFieldValue("")
                            },
                        ) {
                            Icon(
                                Icons.Outlined.Clear,
                                contentDescription = "Clear Max Amount",
                            )
                        }
                    }
                },
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(
                    8.dp,
                    Alignment.CenterHorizontally,
                ),
        ) {
            OutlinedButton(
                onClick = { showStartDatePicker = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(friendlyLocalDate(startDate))
            }
            OutlinedButton(
                onClick = { showEndDatePicker = true },
                modifier = Modifier.weight(1f),
            ) {
                Text(friendlyLocalDate(endDate))
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(onClick = onFilterCanceled) {
                Icon(Icons.Outlined.Clear, contentDescription = "Cancel Filter")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onFilterApplied(
                        descriptionText.text,
                        startDate,
                        endDate,
                        fromAccount?.takeIf { it.isNotBlank() },
                        toAccount?.takeIf { it.isNotBlank() },
                        minAmountText.text.toBigDecimalOrNull(),
                        maxAmountText.text.toBigDecimalOrNull(),
                    )
                },
            ) {
                Icon(Icons.Outlined.Check, contentDescription = "Apply Filter")
            }
        }
    }

    if (showStartDatePicker) {
        Date(
            selectedDate = startDate,
            onDateSelected = { newDate ->
                startDate = newDate
                showStartDatePicker = false
            },
            onDismiss = {
                showStartDatePicker = false
            },
        )
    }

    if (showEndDatePicker) {
        Date(
            selectedDate = endDate,
            onDateSelected = { newDate ->
                endDate = newDate
                showEndDatePicker = false
            },
            onDismiss = {
                showEndDatePicker = false
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Toolbar(
    scrollBehavior: TopAppBarScrollBehavior,
    onBack: () -> Unit,
) {
    TopAppBar(
        colors =
            TopAppBarDefaults
                .centerAlignedTopAppBarColors()
                .copy(scrolledContainerColor = MaterialTheme.colorScheme.background),
        title = { },
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
                    contentDescription = "Back",
                )
            }
        },
        actions = { },
        scrollBehavior = scrollBehavior,
    )
}
