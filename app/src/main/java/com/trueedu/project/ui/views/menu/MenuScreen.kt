package com.trueedu.project.ui.views.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RocketLaunch
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.setting.SettingFragment
import com.trueedu.project.ui.views.spac.SpacListFragment

class MenuScreen(
    private val screen: ScreenControl,
    private val trueAnalytics: TrueAnalytics,
    private val fragmentManager: FragmentManager,
): BottomNavScreen {
    companion object {
        private val TAG = MenuScreen::class.java.simpleName
    }

    @Composable
    override fun Draw() {
        TrueProjectTheme(
            n = screen.theme.intValue,
            forceDark = screen.forceDark.value
        ) {
            Scaffold(
                topBar = {
                    BackTitleTopBar(
                        "메뉴",
                        onBack = null,
                        actionIcon = Icons.Outlined.Search,
                        onAction = {}, // TODO
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.background),
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    MenuItem(Icons.Outlined.Settings, "설정", ::onSettings)
                    MenuItem(Icons.Outlined.RocketLaunch, "스팩 종목 보기", ::onSpacStocks)
                }
            }
        }
    }

    private fun onSettings() {
        trueAnalytics.clickButton("menu__setting__click")
        SettingFragment.show(fragmentManager)
    }

    private fun onSpacStocks() {
        trueAnalytics.clickButton("menu__spac__click")
        SpacListFragment.show(fragmentManager)
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
            .height(56.dp)
    ) {
        Icon(
            modifier = Modifier.size(28.dp),
            imageVector = icon,
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "menu_icon"
        )
        Margin(8)
        TrueText(
            s = text,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    DividerHorizontal()
}
