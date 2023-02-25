package com.ramitsuri.expensereports.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.AccountBox
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.ramitsuri.expensereports.android.ui.AppTheme
import com.ramitsuri.expensereports.android.ui.SettingsNavGraph
import com.ramitsuri.expensereports.android.ui.accounts.AccountsScreen
import com.ramitsuri.expensereports.android.ui.expenses.ExpensesScreen
import com.ramitsuri.expensereports.android.ui.home.HomeScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme(theme) {
                val navController = rememberNavController()
                val showSettingsNavGraph = rememberSaveable { mutableStateOf(false) }
                if (showSettingsNavGraph.value) {
                    SettingsNavGraph(showSettingsNavGraph = showSettingsNavGraph)
                } else {
                    BottomNavGraph(
                        navController = navController,
                        onSettingsRequested = {
                            showSettingsNavGraph.value = true
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomNavGraph(navController: NavHostController, onSettingsRequested: () -> Unit) {
    Scaffold(
        topBar = {
            TopBar(onSettingsRequested = onSettingsRequested)
        },
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = BottomNav.Home.route,
            Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNav.Home.route) { HomeScreen() }
            composable(BottomNav.Accounts.route) { AccountsScreen() }
            composable(BottomNav.Savings.route) { SavingsScreen() }
            composable(BottomNav.Expenses.route) { ExpensesScreen() }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(onSettingsRequested: () -> Unit) {
    TopAppBar(
        title = {},
        actions = {
            MoreMenu(
                onSettingsRequested = onSettingsRequested
            )
        },
        scrollBehavior = enterAlwaysScrollBehavior()
    )
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        bottomNavItems.forEach { item ->
            val isSelected =
                currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                icon = {
                    Icon(
                        if (isSelected) {
                            item.selectedIcon
                        } else {
                            item.unselectedIcon
                        },
                        contentDescription = stringResource(id = item.resourceId)
                    )
                },
                label = { Text(stringResource(item.resourceId)) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}

@Composable
private fun MoreMenu(
    onSettingsRequested: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(
            onClick = {
                expanded = !expanded
            }
        ) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = stringResource(id = R.string.menu_content_description)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            DropdownMenuItem(
                text = { Text(stringResource(id = HomeMenuItem.SETTINGS.textResId)) },
                onClick = {
                    expanded = false
                    onSettingsRequested()
                }
            )
        }
    }
}


sealed class BottomNav(
    val route: String,
    @StringRes val resourceId: Int,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    object Home :
        BottomNav(
            route = "home",
            resourceId = R.string.bottom_nav_home,
            unselectedIcon = Icons.Outlined.Home,
            selectedIcon = Icons.Filled.Home
        )

    object Accounts : BottomNav(
        route = "accounts",
        resourceId = R.string.bottom_nav_accounts,
        unselectedIcon = Icons.Outlined.AccountBox,
        selectedIcon = Icons.Filled.AccountBox
    )

    object Savings : BottomNav(
        route = "savings",
        resourceId = R.string.bottom_nav_savings,
        unselectedIcon = Icons.Outlined.ThumbUp,
        selectedIcon = Icons.Filled.ThumbUp
    )

    object Expenses : BottomNav(
        route = "expenses",
        resourceId = R.string.bottom_nav_expenses,
        unselectedIcon = Icons.Outlined.ShoppingCart,
        selectedIcon = Icons.Filled.ShoppingCart
    )
}

val bottomNavItems = listOf(
    BottomNav.Home,
    BottomNav.Accounts,
    BottomNav.Savings,
    BottomNav.Expenses
)

enum class HomeMenuItem(val id: Int, @StringRes val textResId: Int) {
    SETTINGS(1, R.string.home_menu_settings)
}

@Composable
fun SavingsScreen() {
    Text(text = "Savings")
}