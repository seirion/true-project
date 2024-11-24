package com.trueedu.project.ui.views.setting

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.BuildConfig
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.widget.MySwitch
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ScreenSettingFragment: BaseFragment() {
    companion object {
        private val TAG = ScreenSettingFragment::class.java.simpleName

        fun show(
            fragmentManager: FragmentManager
        ): ScreenSettingFragment {
            val fragment = ScreenSettingFragment()
            fragment.show(fragmentManager, "screen")
            return fragment
        }
    }

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var remoteConfig: RemoteConfig

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("스크린 설정", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                OnOffSetting("다크모드", screen.forceDark.value, ::setForceDarkMode)
                OnOffSetting("항상 화면 켜두기", screen.keepScreenOn.value, ::setKeepScreenOn)
                if (BuildConfig.DEBUG) {
                    OnOffSetting("광고", remoteConfig.adVisible.value, ::setAdVisible)
                }
            }
        }
    }

    private fun setForceDarkMode(on: Boolean) {
        screen.forceDark.value = on
        local.forceDark = on
        trueAnalytics.clickToggleButton("${screenName()}__force_dark__click", !on)
    }

    private fun setKeepScreenOn(on: Boolean) {
        screen.keepScreenOn.value = on
        local.keepScreenOn = on
        trueAnalytics.clickToggleButton("${screenName()}__keep_screen__click", !on)
    }

    private fun setAdVisible(on: Boolean) {
        trueAnalytics.clickToggleButton("${screenName()}__ad_visible__click", !on)
        remoteConfig.setAdVisible(on)
    }
}

@Composable
fun OnOffSetting(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 16.dp)
    ) {
        TrueText(s = title, fontSize = 16, color = MaterialTheme.colorScheme.primary)
        MySwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
