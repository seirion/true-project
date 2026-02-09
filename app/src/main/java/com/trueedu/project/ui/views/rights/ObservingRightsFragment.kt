package com.trueedu.project.ui.views.rights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TrueText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ObservingRightsFragment: BaseFragment() {
    companion object {
        fun show(fragmentManager: FragmentManager): ObservingRightsFragment {
            val fragment = ObservingRightsFragment()
            fragment.show(fragmentManager, "observing_rights")
            return fragment
        }
    }

    private val vm by viewModels<ObservingRightsViewModel>()

    override fun init() {
        super.init()

        vm.fetchRights()
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "권리 현황",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            if (vm.loading.value) {
                LoadingView()
                return@Scaffold
            }

            val result = vm.lastResult.value
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(scrollState)
            ) {
                Button(
                    onClick = {
                        trueAnalytics.clickButton("${screenName()}__refresh__click")
                        vm.fetchRights()
                    },
                ) {
                    TrueText(s = "다시 조회", fontSize = 14, color = MaterialTheme.colorScheme.onPrimary)
                }

                if (result != null) {
                    TrueText(
                        s = result.desc,
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = Int.MAX_VALUE,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                } else {
                    TrueText(
                        s = "권리 현황을 조회합니다.\n(앱키 등록이 필요할 수 있습니다.)",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = Int.MAX_VALUE,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }
}


