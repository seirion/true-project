package com.trueedu.project.ui.spac

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.R
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.RoundedTopColumn
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.views.setting.OnOffSetting
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpacFilterBottomSheet: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager,
        ): SpacFilterBottomSheet {
            return SpacFilterBottomSheet().also {
                it.show(fragmentManager, "spac-filter-sheet")
            }
        }
    }

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var spacManager: SpacManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.PopupDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).also {
            it.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            it.setContent {
                TrueProjectTheme(
                    n = screen.theme.intValue,
                    forceDark = screen.forceDark.value
                ) {
                    Body()
                }
            }
        }
    }

    @Composable
    private fun Body() {
        RoundedTopColumn(
            radius = 20,
            bgColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            TitleView()
            OnOffSetting(
                "청산 시 이자 1년 환산 표시",
                spacManager.spacAnnualProfitMode.value,
                ::setSpacAnnualProfit
            )
        }
    }

    fun setSpacAnnualProfit(on: Boolean) {
        trueAnalytics.clickToggleButton("${screenName()}__spac_annual_profit__click", !on)
        spacManager.setSpacAnnualProfit(on)
    }
}

@Composable
private fun TitleView() {
    TrueText(
        s = "스팩 필터 도구",
        color = MaterialTheme.colorScheme.primary,
        fontSize = 20,
        fontWeight = FontWeight.W600,
        modifier = Modifier
            .padding(16.dp)
            .wrapContentHeight()
            .fillMaxWidth()
    )
}
