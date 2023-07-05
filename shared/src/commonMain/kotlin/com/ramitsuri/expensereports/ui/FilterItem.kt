package com.ramitsuri.expensereports.ui

import com.ramitsuri.expensereports.data.AccountTotal

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
    val name: String = "",
    val fullName: String = "",
    val level: Int = 0,
    val selected: Boolean = false,
) {

    constructor(accountTotal: AccountTotal, level: Int) : this(
        name = accountTotal.name,
        fullName = accountTotal.fullName,
        level = level
    )

    fun isLevelOneChildOf(parentFullName: String): Boolean {
        if (parentFullName == fullName) {
            return false
        }
        return isChildOf(parentFullName) &&
                fullName.removeSuffix(":$name") == parentFullName
    }

    fun isChildOf(parentFullName: String) =
        fullName.startsWith(prefix = parentFullName)
}

fun Account.getStateFromClick(currentState: List<Account>): List<Account> {
    val clickedAccount = this
    val clickedAccountNewSelectedState = !clickedAccount.selected
    // Mark children and account with new selected state
    var newState = currentState.map { account ->
        if (account.fullName == clickedAccount.fullName) {
            account.copy(selected = clickedAccountNewSelectedState)
        } else if (account.isChildOf(clickedAccount.fullName)) {
            account.copy(selected = clickedAccountNewSelectedState)
        } else {
            account
        }
    }
    val root = currentState.first { it.level == 0 }

    var parentFullName = clickedAccount.fullName.removeSuffix(":${clickedAccount.name}")
    if (clickedAccountNewSelectedState) {
        // Selected
        // Mark parent as selected and propagate selection up the generational chain until
        // root account is marked selected or selection no longer needs to be propagated
        var propagate: Boolean
        do {
            val parent = newState.first { it.fullName == parentFullName }
            val selectedChildrenOfParentCount = newState.count { account ->
                account.isLevelOneChildOf(parentFullName) && account.selected
            }
            val childrenOfParentCount = newState.count { account ->
                account.isLevelOneChildOf(parentFullName)
            }
            if (childrenOfParentCount == selectedChildrenOfParentCount) {
                newState = newState.map {
                    if (it.fullName == parentFullName) {
                        it.copy(selected = true)
                    } else {
                        it
                    }
                }
                propagate = true
                parentFullName = parent.fullName.removeSuffix(":${parent.name}")
            } else {
                propagate = false
            }
        } while (parent.fullName != root.fullName && propagate)
    } else {
        // Unselected
        // Mark parent as unselected and propagate un-selection up the generational chain until
        // root account is marked unselected
        do {
            val parent = newState.first { it.fullName == parentFullName }
            newState = newState.map {
                if (it.fullName == parentFullName) {
                    it.copy(selected = false)
                } else {
                    it
                }
            }
            parentFullName = parent.fullName.removeSuffix(":${parent.name}")
        } while (parent.fullName != root.fullName)
    }
    return newState
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
