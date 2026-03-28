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
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Timer
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
import com.trueedu.project.data.DartManager
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.dart.DartListFragment
import com.trueedu.project.ui.ranking.VolumeRankingFragment
import com.trueedu.project.ui.spac.SpacScheduleFragment
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.views.rights.ObservingRightsFragment
import com.trueedu.project.ui.views.schedule.OrderScheduleFragment
import com.trueedu.project.ui.views.setting.SettingFragment

@Composable
fun MenuScreen(
    screen: ScreenControl,
    trueAnalytics: TrueAnalytics,
    tokenKeyManager: TokenKeyManager,
    dartManager: DartManager,
    fragmentManager: FragmentManager,
) {
    TrueProjectTheme(
        n = screen.theme.intValue,
        forceDark = screen.forceDark.value
    ) {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    "메뉴",
                    onBack = null,
                    actionIcon = Icons.Outlined.Settings,
                    onAction = {
                        trueAnalytics.clickButton("menu__setting__click")
                        SettingFragment.show(fragmentManager)
                    },
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
                if (tokenKeyManager.userKey.value != null) {
                    MenuItem(Icons.Outlined.Timer, "예약 매매") {
                        trueAnalytics.clickButton("menu__order_schedule__click")
                        OrderScheduleFragment.show(fragmentManager)
                    }
                    MenuItem(Icons.Outlined.Sync, "권리 현황") {
                        trueAnalytics.clickButton("menu__observing_rights__click")
                        ObservingRightsFragment.show(fragmentManager)
                    }
                }

                val dartCount = dartManager.getSize().let {
                    if (it == 0) "" else " ($it)"
                }
                MenuItem(Icons.Outlined.QueryStats, "스팩 공시${dartCount}") {
                    trueAnalytics.clickButton("menu__spac_dart_list__click")
                    DartListFragment.show(fragmentManager)
                }
                MenuItem(Icons.Outlined.CalendarMonth, "스팩 일정") {
                    trueAnalytics.clickButton("menu__spac_schedule__click")
                    SpacScheduleFragment.show(fragmentManager)
                }
                if (BuildConfig.DEBUG && tokenKeyManager.userKey.value != null) {
                    MenuItem(Icons.Outlined.TrendingUp, "거래량 상위 종목") {
                        trueAnalytics.clickButton("menu__volume_ranking__click")
                        VolumeRankingFragment.show(fragmentManager)
                    }
                }
                if (BuildConfig.DEBUG) {
                    Margin(12)
                    MenuItem(Icons.Outlined.Construction, "어드민 메뉴") {
                        MyAdminFragment.show(fragmentManager)
                    }
                }
            }
        }
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
