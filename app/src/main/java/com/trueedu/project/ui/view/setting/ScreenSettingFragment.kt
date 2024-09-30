package com.trueedu.project.ui.view.setting

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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
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
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    BasicText("강제 다크모드", fontSize = 16, color = MaterialTheme.colorScheme.primary)
                    MySwitch(
                        checked = screen.forceDark.value,
                        onCheckedChange = {
                            screen.forceDark.value = it
                            local.forceDark = it
                            trueAnalytics.clickToggleButton("screen_setting__force_dark__click", !it)
                        }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MySwitch(
    checked: Boolean = true,
    onCheckedChange: (Boolean) -> Unit = {},
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant,
            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
            uncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f),
            disabledCheckedThumbColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            disabledUncheckedThumbColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f),
            disabledCheckedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.12f)
        )
    )

}
