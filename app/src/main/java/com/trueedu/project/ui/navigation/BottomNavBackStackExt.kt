package com.trueedu.project.ui.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.trueedu.project.ui.views.home.BottomNavItem

private inline fun <reified T : BottomNavItem> NavBackStackEntry.parseBottomNavItemOrNull(): BottomNavItem? =
    runCatching { toRoute<T>() }.getOrNull()

fun NavBackStackEntry?.bottomNavItemOrNull(): BottomNavItem? {
    val entry = this ?: return null

    return sequenceOf(
        entry.parseBottomNavItemOrNull<BottomNavItem.Home>(),
        entry.parseBottomNavItemOrNull<BottomNavItem.Watch>(),
        entry.parseBottomNavItemOrNull<BottomNavItem.Spac>(),
        entry.parseBottomNavItemOrNull<BottomNavItem.Menu>(),
    ).filterNotNull().firstOrNull()
}

