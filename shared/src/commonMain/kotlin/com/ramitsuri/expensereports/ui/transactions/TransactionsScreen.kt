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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ramitsuri.expensereports.model.Transaction
import com.ramitsuri.expensereports.model.TransactionSplit
import com.ramitsuri.expensereports.ui.components.DateRangePicker
import com.ramitsuri.expensereports.ui.theme.redColor
import com.ramitsuri.expensereports.utils.format
import com.ramitsuri.expensereports.utils.friendlyLocalDate
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    viewState: TransactionsViewState,
    windowSize: WindowSizeClass,
    onBack: () -> Unit,
    onFilterApplied: (String, LocalDate, LocalDate) -> Unit,
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
                    onFilterApplied = onFilterApplied,
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
                val splitsPartition =
                    remember(transaction) { transaction.splits.partition { it.amount < BigDecimal.ZERO } }
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
                        splitsPartition.first.forEach {
                            Split(it, snackbarHostState)
                        }
                        Icon(Icons.AutoMirrored.Outlined.ArrowForward, contentDescription = "")
                        splitsPartition.second.forEach {
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

@Composable
private fun Filter(
    description: String,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    onFilterApplied: (String, LocalDate, LocalDate) -> Unit,
) {
    var filterExpanded by remember { mutableStateOf(false) }
    AnimatedContent(filterExpanded) { expanded ->
        Row(
            modifier =
                Modifier.fillMaxWidth().padding(16.dp)
                    .clickable(onClick = { filterExpanded = true }),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (expanded) {
                ExpandedFilterContent(
                    description = description,
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
                    onFilterApplied = { description, startDate, endDate ->
                        onFilterApplied(description, startDate, endDate)
                        filterExpanded = false
                    },
                    onFilterCanceled = {
                        filterExpanded = false
                    },
                )
            } else {
                CollapsedFilterContent(
                    description = description,
                    selectedStartDate = selectedStartDate,
                    selectedEndDate = selectedEndDate,
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
    onFilterExpanded: () -> Unit,
) {
    Column(modifier = Modifier.weight(1f)) {
        if (description.isNotEmpty()) {
            Text(description)
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
        Icon(Icons.Outlined.ArrowDropDown, contentDescription = "apply")
    }
}

@Composable
private fun RowScope.ExpandedFilterContent(
    description: String,
    selectedStartDate: LocalDate,
    selectedEndDate: LocalDate,
    onFilterApplied: (String, LocalDate, LocalDate) -> Unit,
    onFilterCanceled: () -> Unit,
) {
    var text by remember { mutableStateOf(TextFieldValue(description)) }
    var startDate by remember { mutableStateOf(selectedStartDate) }
    var endDate by remember { mutableStateOf(selectedEndDate) }
    Column(modifier = Modifier.weight(1f)) {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                if (text.text.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            text = TextFieldValue("")
                        },
                    ) {
                        Icon(Icons.Outlined.Clear, contentDescription = "apply")
                    }
                }
            },
        )
        DateRangePicker(
            modifier = Modifier.weight(1f),
            selectedStartDate = startDate,
            selectedEndDate = endDate,
            onSelectedDateChange = { start, end ->
                startDate = start
                endDate = end
            },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            IconButton(
                onClick = onFilterCanceled,
            ) {
                Icon(Icons.Outlined.Clear, contentDescription = "apply")
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    onFilterApplied(text.text, startDate, endDate)
                },
            ) {
                Icon(Icons.Outlined.Check, contentDescription = "apply")
            }
        }
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
                    contentDescription = "",
                )
            }
        },
        actions = { },
        scrollBehavior = scrollBehavior,
    )
}
