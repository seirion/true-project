package com.trueedu.project.ui.views

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
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.views.setting.SettingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MenuFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): MenuFragment {
            val fragment = MenuFragment()
            fragment.show(fragmentManager, "menu")
            return fragment
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    "메뉴",
                    onBack = ::dismissAllowingStateLoss,
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
            }
        }
    }

    private fun onSettings() {
        trueAnalytics.clickButton("menu__setting__click")
        SettingFragment.show(childFragmentManager)
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
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "menu_icon"
        )
        Margin(8)
        BasicText(
            s = text,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
    }
    DividerHorizontal()
}