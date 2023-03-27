package com.ramitsuri.expensereports.android.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp

@Composable
fun Table(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    verticalLazyListState: LazyListState = rememberLazyListState(),
    horizontalScrollState: ScrollState = rememberScrollState(),
    columnCount: Int,
    rowCount: Int,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }

    Box(modifier = modifier.then(Modifier.horizontalScroll(horizontalScrollState))) {
        LazyColumn(state = verticalLazyListState) {
            items(rowCount) { rowIndex ->
                Column {
                    Row(
                        modifier = if (rowIndex % 2 == 0) {
                            rowModifier.then(Modifier.background(MaterialTheme.colorScheme.surfaceVariant))
                        } else {
                            rowModifier
                        }
                    ) {
                        (0 until columnCount).forEach { columnIndex ->
                            Box(
                                modifier = Modifier
                                    .border(
                                        border = BorderStroke(
                                            1.dp,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                    )
                                    .layout { measurable, constraints ->
                                        val placeable = measurable.measure(constraints)

                                        val existingWidth = columnWidths[columnIndex] ?: 0
                                        val maxWidth = maxOf(existingWidth, placeable.width)

                                        if (maxWidth > existingWidth) {
                                            columnWidths[columnIndex] = maxWidth
                                        }

                                        layout(width = maxWidth, height = placeable.height) {
                                            placeable.placeRelative(0, 0)
                                        }
                                    }) {
                                cellContent(columnIndex, rowIndex)
                            }
                        }
                    }
                }
            }
        }
    }
}