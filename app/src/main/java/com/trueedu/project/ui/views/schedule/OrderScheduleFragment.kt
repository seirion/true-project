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
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.trueedu.project.R
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.worker.DailyWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar
import java.util.concurrent.TimeUnit
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

    fun run() {
        // 현재 시간 가져오기
        val calendar = Calendar.getInstance()
        // 다음 실행 시간을 오전 8:50으로 설정
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 50)
            set(Calendar.SECOND, 0)
        }

        // 만약 현재 시간이 8:50을 지났다면 다음 날로 설정
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 초기 지연 시간 계산 (다음 8:50까지의 시간)
        val initialDelay = calendar.timeInMillis - System.currentTimeMillis()

        // WorkRequest 설정
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyWorker>(
            24, TimeUnit.HOURS) // 24시간마다 반복
            .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS) // 초기 지연
            .build()

        // WorkManager에 작업 등록
        WorkManager.getInstance(requireContext())
            .enqueueUniquePeriodicWork(
                "daily_order_task",
                ExistingPeriodicWorkPolicy.REPLACE,
                dailyWorkRequest
            )
    }
}

private enum class Tab {
    List, // 예약한 전체 예약 목록
    TodayOrders, // 오늘 수행한 결과
}
