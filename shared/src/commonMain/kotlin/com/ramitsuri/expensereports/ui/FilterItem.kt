package com.ramitsuri.expensereports.ui

sealed interface FilterItem {
    val id: Int
    val selected: Boolean
    val displayName: String

    fun duplicate(isSelected: Boolean): FilterItem

    val isAllFilterItem: Boolean
        get() = id == ALL_ID

    companion object {
        const val ALL_ID = -1
    }
}

data class Month(
    val month: Int = 0,
    override val id: Int,
    override val selected: Boolean,
) : FilterItem {
    override val displayName: String
        get() = when (id) {
            FilterItem.ALL_ID -> ALL
            else -> month.string()
        }

    override fun duplicate(isSelected: Boolean): FilterItem {
        return this.copy(selected = isSelected)
    }
}

data class Account(
    val accountName: String = "",
    override val id: Int,
    override val selected: Boolean,
) : FilterItem {
    override val displayName: String
        get() = when (id) {
            FilterItem.ALL_ID -> ALL
            else -> accountName
        }

    override fun duplicate(isSelected: Boolean): FilterItem {
        return this.copy(selected = isSelected)
    }
}

fun getNewItemsOnItemClicked(
    filterItems: List<FilterItem>,
    filterItem: FilterItem
): List<FilterItem> {
    return if (filterItem.isAllFilterItem) {
        filterItems
            .map {
                it.duplicate(isSelected = !filterItem.selected)
            }
    } else {
        val previousSelectionCount = filterItems.count { it.selected }
        /*
         * Non-ALL Filter Unselected
         */
        if (filterItem.selected) {
            // Unselect all filter item as well
            if (previousSelectionCount == filterItems.count()) {
                filterItems
                    .map {
                        it.duplicate(
                            isSelected = if (it.id == filterItem.id || it.isAllFilterItem) {
                                false
                            } else {
                                it.selected
                            }
                        )
                    }
            } else { // Unselect just the filter item
                filterItems
                    .map {
                        it.duplicate(
                            isSelected = if (it.id == filterItem.id) {
                                false
                            } else {
                                it.selected
                            }
                        )
                    }
            }
        } else {
            /*
             * Non-ALL Filter Selected
             */
            // Select all filter item as well since last unselected item was selected
            if (previousSelectionCount == filterItems.count() - 2) {
                filterItems
                    .map {
                        it.duplicate(
                            isSelected = if (it.id == filterItem.id || it.isAllFilterItem) {
                                true
                            } else {
                                it.selected
                            }
                        )
                    }
            } else { // Select just the filter item
                filterItems
                    .map {
                        it.duplicate(
                            isSelected = if (it.id == filterItem.id) {
                                true
                            } else {
                                it.selected
                            }
                        )
                    }
            }
        }
    }
}

private const val ALL = "All"

private fun Int.string(): String {
    return when (this) {
        1 ->
            "Jan"
        2 ->
            "Feb"
        3 ->
            "Mar"
        4 ->
            "Apr"
        5 ->
            "May"
        6 ->
            "Jun"
        7 ->
            "Jul"
        8 ->
            "Aug"
        9 ->
            "Sep"
        10 ->
            "Oct"
        11 ->
            "Nov"
        12 ->
            "Dec"
        else ->
            "Invalid"
    }
}
