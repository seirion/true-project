package com.trueedu.project.ui.views.setting

import android.widget.Toast
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
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.common.DividerHorizontal
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

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

    @Inject
    lateinit var googleAccount: GoogleAccount

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
                    AppKeyInputFragment.show(false, parentFragmentManager)
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

                SettingLabel("버전", BuildConfig.VERSION_NAME)

                SettingItem("탈퇴 및 데이터 삭제", googleAccount.loggedIn()) {
                    trueAnalytics.enterView("setting__withdraw__click")
                    vm.withdraw(
                        onSuccess = {
                            Toast.makeText(requireContext(), "계정 삭제 완료되었습니다", Toast.LENGTH_SHORT).show()
                        },
                        onFail = {
                            Toast.makeText(requireContext(), "오류가 발생하였습니다", Toast.LENGTH_SHORT).show()
                        },
                    )
                    googleAccount.revokeAccess(requireContext())
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
        TrueText(
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

@Preview(showBackground = true)
@Composable
fun SettingLabel(
    title: String = "Version",
    value: String = "1.0.0",
    enabled: Boolean = true,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(56.dp)
    ) {
        TrueText(
            s = title,
            fontSize = 16,
            color = MaterialTheme.colorScheme.primary,
        )
        TrueText(
            s = value,
            fontSize = 18,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
    DividerHorizontal()
}
