package com.tp.tpapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tp.tpapp.ui.screen.AccountEditScreen
import com.tp.tpapp.ui.screen.AppDetailScreen
import com.tp.tpapp.ui.screen.AppListScreen
import com.tp.tpapp.ui.screen.PasswordRecycleBinScreen
import com.tp.tpapp.ui.screen.RecordEditScreen
import com.tp.tpapp.ui.screen.RecordListScreen
import com.tp.tpapp.ui.screen.RecycleBinScreen
import com.tp.tpapp.ui.screen.SettingsScreen

object Routes {
    const val RECORD_LIST = "record_list"
    const val APP_LIST = "app_list"
    const val RECORD_EDIT = "record_edit/{recordId}"
    const val RECORD_RECYCLE_BIN = "record_recycle_bin"
    const val PASSWORD_RECYCLE_BIN = "password_recycle_bin"
    const val SETTINGS = "settings"
    const val APP_DETAIL = "app_detail/{appId}"
    const val ACCOUNT_EDIT = "account_edit/{appId}?accountId={accountId}"

    fun recordEdit(recordId: Long) = "record_edit/$recordId"
    fun appDetail(appId: Long) = "app_detail/$appId"
    fun accountEdit(appId: Long, accountId: Long? = null): String {
        return if (accountId != null) {
            "account_edit/$appId?accountId=$accountId"
        } else {
            "account_edit/$appId"
        }
    }
}

@Composable
fun TPNavigation(startDestination: String = Routes.RECORD_LIST) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // 判断是否在顶级页面（需要显示底部导航栏）
    val showBottomBar = currentDestination?.route in listOf(
        Routes.RECORD_LIST,
        Routes.APP_LIST
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    // 记录 Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Description, contentDescription = "记录") },
                        label = { Text("记录") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.RECORD_LIST } == true,
                        onClick = {
                            navController.navigate(Routes.RECORD_LIST) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    // 密码 Tab
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Lock, contentDescription = "密码") },
                        label = { Text("密码") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.APP_LIST } == true,
                        onClick = {
                            navController.navigate(Routes.APP_LIST) {
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
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(bottom = if (showBottomBar) innerPadding.calculateBottomPadding() else innerPadding.calculateBottomPadding())
            // padding only apply bottom padding, otherwise it will add top spacing on all screens. Actually, just using `innerPadding` directly is fine but it might pad the top. Since topBar is not in Scaffold, it's fine. Wait, topBar is NOT in this Scaffold. 
        ) {
            composable(Routes.RECORD_LIST) {
                RecordListScreen(
                    onEditRecord = { recordId ->
                        navController.navigate(Routes.recordEdit(recordId))
                    },
                    onOpenRecycleBin = {
                        navController.navigate(Routes.RECORD_RECYCLE_BIN)
                    },
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }

            composable(Routes.APP_LIST) {
                AppListScreen(
                    onAppClick = { appId ->
                        navController.navigate(Routes.appDetail(appId))
                    },
                    onNavigateToAccountEdit = { appId ->
                        navController.navigate(Routes.accountEdit(appId))
                    },
                    onOpenRecycleBin = {
                        navController.navigate(Routes.PASSWORD_RECYCLE_BIN)
                    },
                    onOpenSettings = {
                        navController.navigate(Routes.SETTINGS)
                    }
                )
            }

            composable(
                route = Routes.RECORD_EDIT,
                arguments = listOf(navArgument("recordId") { type = NavType.LongType })
            ) { backStackEntry ->
                val recordId = backStackEntry.arguments?.getLong("recordId") ?: return@composable
                RecordEditScreen(
                    recordId = recordId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.RECORD_RECYCLE_BIN) {
                RecycleBinScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.PASSWORD_RECYCLE_BIN) {
                PasswordRecycleBinScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Routes.SETTINGS) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.APP_DETAIL,
                arguments = listOf(navArgument("appId") { type = NavType.LongType })
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getLong("appId") ?: return@composable
                AppDetailScreen(
                    appId = appId,
                    onNavigateBack = { navController.popBackStack() },
                    onAddAccount = { navController.navigate(Routes.accountEdit(appId)) },
                    onEditAccount = { accountId ->
                        navController.navigate(Routes.accountEdit(appId, accountId))
                    }
                )
            }

            composable(
                route = Routes.ACCOUNT_EDIT,
                arguments = listOf(
                    navArgument("appId") { type = NavType.LongType },
                    navArgument("accountId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                )
            ) { backStackEntry ->
                val appId = backStackEntry.arguments?.getLong("appId") ?: return@composable
                val accountId = backStackEntry.arguments?.getLong("accountId")?.takeIf { it > 0 }
                AccountEditScreen(
                    appId = appId,
                    accountId = accountId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
