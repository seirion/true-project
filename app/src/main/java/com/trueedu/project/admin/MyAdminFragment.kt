package com.trueedu.project.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Construction
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import com.trueedu.project.BuildConfig
import com.trueedu.project.admin.spac.SpacAdminFragment
import com.trueedu.project.admin.spac.SpacScheduleAdminFragment
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.spac.SpacAnalysisFragment
import com.trueedu.project.ui.views.menu.MenuItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MyAdminFragment : BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): MyAdminFragment {
            return MyAdminFragment().also {
                it.show(fragmentManager, "my-admin")
            }
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = { BackTitleTopBar("어드민 메뉴", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (BuildConfig.DEBUG) {
                    MenuItem(Icons.Outlined.Construction, "스팩 어드민", ::onSpacAdmin)
                    MenuItem(Icons.Outlined.Construction, "스팩 스케쥴 어드민", ::onSpacScheduleAdmin)
                    MenuItem(Icons.Outlined.QueryStats, "스팩 분석", ::onSpacAnalysis)
                }
            }
        }
    }

    private fun onSpacAdmin() {
        SpacAdminFragment.show(parentFragmentManager)
    }

    private fun onSpacScheduleAdmin() {
        SpacScheduleAdminFragment.show(parentFragmentManager)
    }

    private fun onSpacAnalysis() {
        SpacAnalysisFragment.show(parentFragmentManager)
    }
}
