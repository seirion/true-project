package com.trueedu.project.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Home
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

    fun icon(selected: Boolean) = if (selected) iconSelected else iconNormal
}