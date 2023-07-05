package com.ramitsuri.expensereports.android.ui.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ramitsuri.expensereports.android.R
import com.ramitsuri.expensereports.ui.Account
import com.ramitsuri.expensereports.ui.getStateFromClick

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsFilterDialog(
    accounts: List<Account>,
    onAccountFiltersApplied: (List<Account>) -> Unit,
    modifier: Modifier = Modifier,
    dialogState: MutableState<Boolean>
) {
    var selectionState by rememberSaveable { mutableStateOf(accounts) }

    Dialog(
        onDismissRequest = { dialogState.value = false },
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Card(modifier = modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .fillMaxHeight(0.8f)
                    .padding(16.dp)
            ) {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, false)
                ) {
                    selectionState.forEach { account ->
                        item {
                            Row(
                                modifier = Modifier
                                    .clickable {
                                        selectionState = account.getStateFromClick(selectionState)
                                    }
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(account.level) {
                                    Spacer(modifier = Modifier.width(16.dp))
                                }
                                Icon(
                                    imageVector = if (account.selected) {
                                        Icons.Filled.CheckBox
                                    } else {
                                        Icons.Filled.CheckBoxOutlineBlank
                                    },
                                    contentDescription = null,
                                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = {
                        dialogState.value = false
                    }) {
                        Text(text = stringResource(id = R.string.cancel))
                    }
                    TextButton(onClick = {
                        onAccountFiltersApplied(selectionState)
                        dialogState.value = false
                    }) {
                        Text(text = stringResource(id = R.string.apply))
                    }
                }
            }
        }
    }
}