package com.ramitsuri.expensereports.android.ui.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ionspin.kotlin.bignum.decimal.BigDecimal
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.android.utils.format
import com.ramitsuri.expensereports.android.utils.timeAndDay
import com.ramitsuri.expensereports.viewmodel.Account
import com.ramitsuri.expensereports.viewmodel.AccountBalance
import com.ramitsuri.expensereports.viewmodel.AccountType
import com.ramitsuri.expensereports.viewmodel.AccountsViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.androidx.compose.getViewModel

@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = getViewModel()
) {
    val viewState = viewModel.state.collectAsState().value
    AccountsContent(viewState.accounts)
}

@Composable
private fun AccountsContent(
    accounts: Map<AccountType, Account>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            accounts.forEach { (type, account) ->
                fullSpanItem {
                    AccountTypeHeader(type, account.asOf)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(account.accountBalances) { accountBalance ->
                    AccountItem(accountBalance)
                }
                fullSpanItem {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

private fun LazyGridScope.fullSpanItem(
    content: @Composable LazyGridItemScope.() -> Unit
) {
    item(span = {
        GridItemSpan(this.maxLineSpan)
    }, content = content)
}


@Composable
private fun AccountTypeHeader(
    type: AccountType,
    asOf: LocalDateTime,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    now: LocalDateTime = Clock.System.now().toLocalDateTime(timeZone)
) {
    Column(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(
                        topEnd = 24.dp,
                        bottomEnd = 24.dp
                    )
                )
                .padding(8.dp)
        ) {
            val text = if (type == AccountType.ASSET) {
                stringResource(id = R.string.accounts_assets_title)
            } else {
                stringResource(id = R.string.accounts_liabilities_title)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = asOf.timeAndDay(now = now),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun AccountItem(
    accountBalance: AccountBalance,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
    ) {
        Card(
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = accountBalance.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = accountBalance.balance.format(),
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}

@Preview
@Composable
private fun AccountBalanceCardItem() {
    Surface(Modifier.fillMaxSize()) {
        AccountItem(
            accountBalance = AccountBalance(
                "Credit Card",
                BigDecimal.parseString("4212.54")
            ),
            modifier = Modifier.size(200.dp)
        )
    }
}

@Preview
@Composable
private fun HeaderItem1() {
    Surface(Modifier.fillMaxSize()) {
        AccountTypeHeader(
            type = AccountType.ASSET,
            asOf = LocalDateTime.parse("2023-02-24T11:30:00"),
            timeZone = TimeZone.UTC,
            now = LocalDateTime.parse("2023-02-24T11:45:00")
        )
    }
}

@Preview
@Composable
private fun HeaderItem2() {
    Surface(Modifier.fillMaxSize()) {
        AccountTypeHeader(
            type = AccountType.ASSET,
            asOf = LocalDateTime.parse("2023-02-23T11:30:00"),
            timeZone = TimeZone.UTC,
            now = LocalDateTime.parse("2023-02-24T11:45:00")
        )
    }
}

@Preview
@Composable
private fun HeaderItem3() {
    Surface(Modifier.fillMaxSize()) {
        AccountTypeHeader(
            type = AccountType.ASSET,
            asOf = LocalDateTime.parse("2023-02-22T11:30:00"),
            timeZone = TimeZone.UTC,
            now = LocalDateTime.parse("2023-02-24T11:45:00")
        )
    }
}

@Preview
@Composable
private fun HeaderItem4() {
    Surface(Modifier.fillMaxSize()) {
        AccountTypeHeader(
            type = AccountType.ASSET,
            asOf = LocalDateTime.parse("2022-02-24T11:30:00"),
            timeZone = TimeZone.UTC,
            now = LocalDateTime.parse("2023-02-24T11:45:00")
        )
    }
}

@Preview
@Composable
private fun HeaderItem5() {
    Surface(Modifier.fillMaxSize()) {
        AccountTypeHeader(
            type = AccountType.ASSET,
            asOf = LocalDateTime.parse("2023-02-24T11:30:00"),
            timeZone = TimeZone.UTC,
            now = LocalDateTime.parse("2023-02-24T11:45:00")
        )
    }
}