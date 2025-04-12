package com.ramitsuri.expensereports.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material3.DateRangePicker as AndroidDateRangePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePicker(
    modifier: Modifier = Modifier,
    selectedStartDate: LocalDate?,
    selectedEndDate: LocalDate?,
    onSelectedDateChange: (LocalDate, LocalDate) -> Unit,
) {
    fun Long.toLocalDate(): LocalDate {
        return Instant
            .fromEpochMilliseconds(this)
            .toLocalDateTime(TimeZone.UTC)
            .date
    }

    fun LocalDate.toMillisSinceEpoch(): Long {
        return this
            .atStartOfDayIn(TimeZone.UTC)
            .toEpochMilliseconds()
    }

    val state =
        rememberDateRangePickerState(
            initialSelectedStartDateMillis = selectedStartDate?.toMillisSinceEpoch(),
            initialSelectedEndDateMillis = selectedEndDate?.toMillisSinceEpoch(),
        )
    LaunchedEffect(state.selectedEndDateMillis, state.selectedStartDateMillis) {
        val startDate = state.selectedStartDateMillis
        val endDate = state.selectedEndDateMillis
        if (startDate != null && endDate != null) {
            onSelectedDateChange(
                startDate.toLocalDate(),
                endDate.toLocalDate(),
            )
        }
    }
    AndroidDateRangePicker(
        modifier = modifier,
        state = state,
        title = null,
        headline = null,
        showModeToggle = false,
    )
}
