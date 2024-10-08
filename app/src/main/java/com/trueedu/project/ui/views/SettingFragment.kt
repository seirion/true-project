package com.trueedu.project.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.BuildConfig
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.ui.views.setting.ColorPaletteFragmentFragment
import com.trueedu.project.ui.views.setting.ScreenSettingFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SettingFragment {
            val fragment = SettingFragment()
            fragment.show(fragmentManager, "setting")
            return fragment
        }
    }

    private val vm by viewModels<SettingViewModel>()

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("설정", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SettingItem("appkey 설정", true) {
                    trueAnalytics.enterView("setting__appkey_setting__click")
                    AppKeyInputFragment.show(parentFragmentManager)
                }
                SettingItem("Screen 설정", true) {
                    trueAnalytics.enterView("setting__screen_setting__click")
                    ScreenSettingFragment.show(parentFragmentManager)
                }

                val label = "종목 정보 업데이트" + vm.stockUpdateLabel.value
                SettingItem(label, vm.updateAvailable.value) {
                    trueAnalytics.enterView("setting__update_stock_info__click")
                    vm.updateStocks()
                }

                if (BuildConfig.DEBUG) {
                    SettingItem("color scheme", true) {
                        ColorPaletteFragmentFragment.show(parentFragmentManager)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingItem(
    text: String = "나의 설정",
    enabled: Boolean = true,
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 10.dp)
            .height(56.dp)
    ) {
        BasicText(
            s = text,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
        Icon(
            modifier = Modifier.size(28.dp),
            imageVector = Icons.Outlined.ChevronRight,
            tint = MaterialTheme.colorScheme.tertiary,
            contentDescription = "next"
        )
    }
    DividerHorizontal()
}
