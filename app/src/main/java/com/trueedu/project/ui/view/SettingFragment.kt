package com.trueedu.project.ui.view

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.treuedu.project.BuildConfig
import com.treuedu.project.R
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.view.setting.AppKeyInputFragment
import com.trueedu.project.ui.view.setting.ColorPaletteFragmentFragment
import com.trueedu.project.ui.view.setting.ScreenSettingFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment: BottomSheetDialogFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SettingFragment {
            val fragment = SettingFragment()
            fragment.show(fragmentManager, "setting")
            return fragment
        }
    }

    @Inject
    lateinit var screen: ScreenControl
    @Inject
    lateinit var trueAnalytics: TrueAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FillScreenSheetTheme)

        trueAnalytics.enterView("setting__enter")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return super.onCreateDialog(savedInstanceState).apply {
            (this as? BottomSheetDialog)?.behavior?.run {
                this.skipCollapsed = true
                this.state = BottomSheetBehavior.STATE_EXPANDED
                this.isDraggable = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setContent {
                TrueProjectTheme(
                    n = screen.theme.intValue,
                    forceDark = screen.forceDark.value
                ) {
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
                            SettingItem("appkey 설정") {
                                trueAnalytics.enterView("setting__appkey_setting__click")
                                AppKeyInputFragment.show(parentFragmentManager)
                            }
                            SettingItem("Screen 설정") {
                                trueAnalytics.enterView("setting__screen_setting__click")
                                ScreenSettingFragment.show(parentFragmentManager)
                            }

                            if (BuildConfig.DEBUG) {
                                SettingItem("color scheme") {
                                    ColorPaletteFragmentFragment.show(parentFragmentManager)
                                }
                            }
                        }
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
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
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
