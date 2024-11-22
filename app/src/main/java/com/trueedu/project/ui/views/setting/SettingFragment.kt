package com.trueedu.project.ui.views.setting

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.BuildConfig
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.ButtonAction
import com.trueedu.project.ui.common.PopupFragment
import com.trueedu.project.ui.common.PopupType
import com.trueedu.project.ui.widget.SettingItem
import com.trueedu.project.ui.widget.SettingLabel
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
                    trueAnalytics.enterView("${screenName()}__appkey_setting__click")
                    AppKeyInputFragment.show(false, parentFragmentManager)
                }
                SettingItem("Screen 설정", true) {
                    trueAnalytics.enterView("${screenName()}__screen_setting__click")
                    ScreenSettingFragment.show(parentFragmentManager)
                }
                SettingItem("스팩 설정", true) {
                    trueAnalytics.enterView("${screenName()}__spac_setting__click")
                    SpacSettingFragment.show(parentFragmentManager)
                }

                val label = "종목 정보 업데이트" + vm.stockUpdateLabel.value
                SettingItem(label, vm.updateAvailable.value) {
                    trueAnalytics.enterView("${screenName()}__update_stock_info__click")
                    vm.updateStocks()
                }

                if (BuildConfig.DEBUG) {
                    SettingItem("color scheme", true) {
                        ColorPaletteFragmentFragment.show(parentFragmentManager)
                    }
                }

                SettingLabel("버전", BuildConfig.VERSION_NAME, true, ::gotoPlayStore)

                SettingItem("탈퇴 및 데이터 삭제", googleAccount.loggedIn()) {
                    trueAnalytics.enterView("${screenName()}__withdraw__click")
                    showWithdrawPopup()
                }
            }
        }
    }

    private fun showWithdrawPopup() {
        val deleteFun: () -> Unit = {
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
        PopupFragment.show(
            title = "계정 삭제",
            desc = "계정의 모든 데이터가 삭제되며, 삭제된 데이터는 복구할 수 없습니다. 삭제하시겠습니까?",
            popupType = PopupType.DELETE_CANCEL,
            buttonActions = listOf(
                ButtonAction(label = "삭제", onClick = deleteFun),
                ButtonAction(label = "취소", onClick = {}),
            ),
            cancellable = true,
            fragmentManager = parentFragmentManager,
        )
    }

    private fun gotoPlayStore() {
        trueAnalytics.enterView("${screenName()}__version__click")
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                data =
                    Uri.parse("https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}")
            }
        )
    }
}
