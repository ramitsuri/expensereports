package com.ramitsuri.expensereports.android.ui.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Table(
    modifier: Modifier = Modifier,
    columnCount: Int,
    rowCount: Int,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit
) {
    val columnWidths = remember { mutableStateMapOf<Int, Int>() }
    var maxHeight = remember { 0 }

    val horizontalLazyListState: LazyListState = rememberLazyListState()
    val verticalScrollState: ScrollState = rememberScrollState()

    Box(modifier = modifier.then(Modifier.verticalScroll(verticalScrollState))) {
        LazyRow(state = horizontalLazyListState) {
            stickyHeader {
                Column {
                    (0 until rowCount).forEach { rowIndex ->
                        val boxModifier = if (rowIndex % 2 == 0) {
                            Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
                        } else {
                            Modifier.background(MaterialTheme.colorScheme.surface)
                        }
                        Box(
                            modifier = boxModifier
                                .border(
                                    border = BorderStroke(
                                        1.dp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                )
                                .layout { measurable, constraints ->
                                    val placeable = measurable.measure(constraints)

                                    val existingWidth = columnWidths[0] ?: 0
                                    val maxWidth = maxOf(existingWidth, placeable.width)

                                    if (maxWidth > existingWidth) {
                                        columnWidths[0] = maxWidth
                                    }

                                    if (placeable.height > maxHeight) {
                                        maxHeight = placeable.height
                                    }

                                    layout(width = maxWidth, height = maxHeight) {
                                        placeable.placeRelative(0, 0)
                                    }
                                }) {
                            cellContent(0, rowIndex)
                        }
                    }
                }
            }
            items(columnCount - 1) { column ->
                val columnIndex = column + 1 // Because first column is sticky, shown above
                Column {
                    (0 until rowCount).forEach { rowIndex ->
                        CellContent(
                            rowIndex = rowIndex,
                            columnIndex = columnIndex,
                            maxHeight = maxHeight,
                            updateMaxHeight = {maxHeight = it},
                            columnWidths = columnWidths,
                            cellContent = cellContent
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CellContent(
    rowIndex: Int,
    columnIndex: Int,
    maxHeight: Int,
    updateMaxHeight: (Int) -> Unit,
    columnWidths: SnapshotStateMap<Int, Int>,
    cellContent: @Composable (columnIndex: Int, rowIndex: Int) -> Unit
) {
    val boxModifier = if (rowIndex % 2 == 0) {
        Modifier.background(MaterialTheme.colorScheme.surfaceVariant)
    } else {
        Modifier.background(MaterialTheme.colorScheme.surface)
    }
    Box(
        modifier = boxModifier
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

                if (placeable.height > maxHeight) {
                    updateMaxHeight(placeable.height)
                }

                layout(width = maxWidth, height = maxHeight) {
                    placeable.placeRelative(0, 0)
                }
            }) {
        cellContent(columnIndex, rowIndex)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TableCell(
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