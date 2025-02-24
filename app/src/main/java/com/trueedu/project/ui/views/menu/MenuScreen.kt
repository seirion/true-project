package com.trueedu.project.ui.views.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.BuildConfig
import com.trueedu.project.admin.MyAdminFragment
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.ranking.VolumeRankingFragment
import com.trueedu.project.ui.spac.SpacScheduleFragment
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.setting.SettingFragment

class MenuScreen(
    private val screen: ScreenControl,
    private val trueAnalytics: TrueAnalytics,
    private val tokenKeyManager: TokenKeyManager,
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
                        onAction = null, // TODO
                    )
                },
                contentWindowInsets =
                    ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
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
                    MenuItem(Icons.Outlined.CalendarMonth, "스팩 일정", ::onSpacSchedule)
                    if (tokenKeyManager.userKey.value != null) {
                        MenuItem(Icons.Outlined.TrendingUp, "거래량 상위 종목", ::onVolumeRanking)
                    }
                    if (BuildConfig.DEBUG) {
                        Margin(12)
                        MenuItem(Icons.Outlined.Construction, "어드민 메뉴", ::onAdminMenu)
                    }
                }
            }
        }
    }

    private fun onSettings() {
        trueAnalytics.clickButton("${screenName()}__setting__click")
        SettingFragment.show(fragmentManager)
    }

    private fun onSpacSchedule() {
        trueAnalytics.clickButton("${screenName()}__spac_schedule__click")
        SpacScheduleFragment.show(fragmentManager)
    }

    private fun onVolumeRanking() {
        trueAnalytics.clickButton("${screenName()}__volume_ranking__click")
        VolumeRankingFragment.show(fragmentManager)
    }

    private fun onAdminMenu() {
        MyAdminFragment.show(fragmentManager)
    }
}

@Composable
fun MenuItem(
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
