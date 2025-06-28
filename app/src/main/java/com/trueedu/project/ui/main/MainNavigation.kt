package com.trueedu.project.ui.main

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.trueedu.project.ui.views.home.BottomNavItem
import com.trueedu.project.ui.views.home.HomeScreen
import com.trueedu.project.ui.views.menu.MenuScreen
import com.trueedu.project.ui.views.spac.SpacScreen
import com.trueedu.project.ui.views.watch.WatchScreen


@Composable
fun MainNavigation(
    navController: NavHostController,
    innerPadding: PaddingValues,
    homeScreen: HomeScreen,
    watchScreen: WatchScreen,
    spacScreen: SpacScreen,
    menuScreen: MenuScreen,
) {
    NavHost(
        navController = navController,
        startDestination = BottomNavItem.Home.screenRoute,
        modifier = Modifier.padding(innerPadding)
    ) {
        composable(BottomNavItem.Home.screenRoute) {
            homeScreen.Draw()
        }
        composable(BottomNavItem.Watch.screenRoute) {
            watchScreen.Draw()
        }
        composable(BottomNavItem.Spac.screenRoute) {
            spacScreen.Draw()
        }
        composable(BottomNavItem.Menu.screenRoute) {
            menuScreen.Draw()
        }
    }
}
