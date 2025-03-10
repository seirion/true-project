package com.trueedu.project.ui.views.schedule

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.R
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderScheduleFragment: BaseFragment() {
    companion object {
        fun show(fragmentManager: FragmentManager): OrderScheduleFragment {
            val fragment = OrderScheduleFragment()
            fragment.show(fragmentManager, "order_schedule")
            return fragment
        }
    }

    private val vm by viewModels<OrderScheduleViewModel>()

    @Inject
    lateinit var local: Local

    private val currentTab = mutableStateOf(Tab.List)

    private fun setOrderTab(tab: Tab) {
        currentTab.value = tab
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    override fun init() {
        super.init()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar("주문 예약", ::dismissAllowingStateLoss)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
            }
        }
    }

    @Composable
    private fun TabViews() {
        val currentTabIndex = currentTab.value.ordinal

        TabRow(
            selectedTabIndex = 0,
            modifier = Modifier
                .padding(horizontal = 2.dp, vertical = 4.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.background,
            indicator = { tabPositions ->
                val currentTabItem = tabPositions[currentTabIndex]
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(currentTabItem)
                )
            },
            divider = {},
        ) {
        }
    }
}

private enum class Tab {
    List, // 예약한 전체 예약 목록
    TodayOrders, // 오늘 수행한 결과
}
