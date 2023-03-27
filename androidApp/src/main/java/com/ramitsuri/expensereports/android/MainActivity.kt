package com.ramitsuri.expensereports.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
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
import com.ramitsuri.expensereports.android.ui.home.HomeScreen
import com.ramitsuri.expensereports.android.ui.reports.ReportsScreen
import com.ramitsuri.expensereports.android.ui.settings.SettingsScreen
import com.ramitsuri.expensereports.android.ui.transactions.TransactionsScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AppTheme(theme) {
                val navController = rememberNavController()
                BottomNavGraph(navController = navController)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomNavGraph(navController: NavHostController) {
    Scaffold(
        topBar = {},
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
            composable(BottomNav.Reports.route) { ReportsScreen() }
            composable(BottomNav.Transactions.route) { TransactionsScreen() }
            composable(BottomNav.Misc.route) { SettingsScreen() }
        }
    }
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
                        painterResource(
                            id = if (isSelected) {
                                item.selectedIcon
                            } else {
                                item.unselectedIcon
                            }
                        ),
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
                        restoreState = false
                    }
                }
            )
        }
    }
}

sealed class BottomNav(
    val route: String,
    @StringRes
    val resourceId: Int,
    @DrawableRes
    val unselectedIcon: Int,
    @DrawableRes
    val selectedIcon: Int
) {
    object Home :
        BottomNav(
            route = "home",
            resourceId = R.string.bottom_nav_home,
            unselectedIcon = R.drawable.home_outlined,
            selectedIcon = R.drawable.home_filled
        )

    object Reports : BottomNav(
        route = "reports",
        resourceId = R.string.bottom_nav_reports,
        unselectedIcon = R.drawable.reports_outlined,
        selectedIcon = R.drawable.reports_filled
    )

    object Transactions : BottomNav(
        route = "transactions",
        resourceId = R.string.bottom_nav_transactions,
        unselectedIcon = R.drawable.transactions_outlined,
        selectedIcon = R.drawable.transactions_filled
    )

    object Misc : BottomNav(
        route = "misc",
        resourceId = R.string.bottom_nav_misc,
        unselectedIcon = R.drawable.misc_outlined,
        selectedIcon = R.drawable.misc_filled
    )
}

val bottomNavItems = listOf(
    BottomNav.Home,
    BottomNav.Reports,
    BottomNav.Transactions,
    BottomNav.Misc
)