package com.trueedu.project.ui.views.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpacSettingFragment: BaseFragment() {
    companion object {
        private val TAG = SpacSettingFragment::class.java.simpleName

        fun show(
            fragmentManager: FragmentManager
        ): SpacSettingFragment {
            val fragment = SpacSettingFragment()
            fragment.show(fragmentManager, "screen")
            return fragment
        }
    }

    @Inject
    lateinit var local: Local

    private val spacAnnualProfit = mutableStateOf(true)

    override fun init() {
        super.init()
        spacAnnualProfit.value = local.spacAnnualProfit
    }

    fun setSpacAnnualProfit(on: Boolean) {
        trueAnalytics.clickToggleButton("${screenName()}__spac_annual_profit__click", !on)
        spacAnnualProfit.value = on
        local.spacAnnualProfit = on
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("spac 설정", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                OnOffSetting("청산 시 이자 1년 환산 표시", spacAnnualProfit.value, ::setSpacAnnualProfit)
            }
        }
    }
}
