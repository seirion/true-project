package com.trueedu.project.ui.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.trueedu.project.ui.views.home.BottomNavItem

fun NavBackStackEntry?.bottomNavItemOrNull(): BottomNavItem? {
    return runCatching { this?.toRoute<BottomNavItem>() }.getOrNull()
}

