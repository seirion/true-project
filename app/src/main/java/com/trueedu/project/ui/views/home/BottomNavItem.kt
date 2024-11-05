package com.trueedu.project.ui.views.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val title: String,
    val iconSelected: ImageVector,
    val iconNormal: ImageVector,
    val screenRoute: String
) {
    data object Home : BottomNavItem(
        "홈",
        Icons.Filled.Home,
        Icons.Outlined.Home,
        "home"
    )
    data object Watch: BottomNavItem(
        "관심",
        Icons.Filled.Star,
        Icons.Outlined.StarOutline,
        "watch"
    )
    data object Menu: BottomNavItem(
        "더보기",
        Icons.Filled.Menu,
        Icons.Outlined.Menu,
        "menu"
    )

    fun icon(selected: Boolean) = if (selected) iconSelected else iconNormal
}