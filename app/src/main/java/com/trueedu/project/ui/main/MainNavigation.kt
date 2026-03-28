package com.trueedu.project.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.DartManager
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.views.home.BottomNavItem
import com.trueedu.project.ui.views.home.HomeScreen
import com.trueedu.project.ui.views.menu.MenuScreen
import com.trueedu.project.ui.views.spac.SpacScreen
import com.trueedu.project.ui.views.watch.WatchScreen


@Composable
fun MainNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    fragmentManager: FragmentManager,
    stockPool: StockPool,
    admobManager: AdmobManager,
    remoteConfig: RemoteConfig,
    trueAnalytics: TrueAnalytics,
    screen: ScreenControl,
    tokenKeyManager: TokenKeyManager,
    dartManager: DartManager,
    onUserInfo: () -> Unit,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home,
        modifier = Modifier.padding(innerPadding),
    ) {
        composable<BottomNavItem.Home> {
            HomeScreen(
                stockPool = stockPool,
                admobManager = admobManager,
                remoteConfig = remoteConfig,
                trueAnalytics = trueAnalytics,
                fragmentManager = fragmentManager,
                onUserInfo = onUserInfo,
            )
        }
        composable<BottomNavItem.Watch> {
            WatchScreen(
                admobManager = admobManager,
                remoteConfig = remoteConfig,
                trueAnalytics = trueAnalytics,
                fragmentManager = fragmentManager,
            )
        }
        composable<BottomNavItem.Spac> {
            SpacScreen(
                trueAnalytics = trueAnalytics,
                remoteConfig = remoteConfig,
                admobManager = admobManager,
                fragmentManager = fragmentManager,
            )
        }
        composable<BottomNavItem.Menu> {
            MenuScreen(
                screen = screen,
                trueAnalytics = trueAnalytics,
                tokenKeyManager = tokenKeyManager,
                dartManager = dartManager,
                fragmentManager = fragmentManager,
            )
        }
    }
}
