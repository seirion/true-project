package com.trueedu.project.ui.views.schedule

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.RemoveCircle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.R
import com.trueedu.project.model.dto.order.ScheduleOrderResultDetail
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.dateFormat
import com.trueedu.project.utils.formatter.intFormatter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OrderScheduleFragment: BaseFragment() {
    companion object {
        private val TAG = OrderScheduleFragment::class.java.simpleName

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
                val onAction = if (currentTab.value == Tab.List) {
                    ::onAdd
                } else {
                    null
                }
                BackTitleTopBar(
                    title = "주문 예약",
                    onBack = ::dismissAllowingStateLoss,
                    actionIcon = Icons.Outlined.Add,
                    onAction = onAction,
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

            val scrollState = rememberScrollState()
            val list = vm.list.value?.list ?: emptyList()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(top = 12.dp)
                    .verticalScroll(scrollState)
            ) {
                ScheduleSummary(list.size)
                list.forEachIndexed { index, it ->
                    ScheduleItem(
                        item = it,
                        onClick = { onModify() },
                        onRemove = { onRemove(index) },
                    )
                }
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

    private fun onAdd() {
        trueAnalytics.clickButton("${screenName()}__add__click")
        ScheduleAddFragment.show(parentFragmentManager) {
            vm.add(it) { errorMessage ->
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun onModify() {
        trueAnalytics.clickButton("${screenName()}__modify__click")
    }

    private fun onRemove(index: Int) {
        trueAnalytics.clickButton("${screenName()}__remove__click")
        vm.removeAt(index) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}

private enum class Tab {
    List, // 예약한 전체 예약 목록
    TodayOrders, // 오늘 수행한 결과
}

@Preview(showBackground = true)
@Composable
private fun ScheduleSummary(
    count: Int = 1,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .height(32.dp)
    ) {
        TrueText(
            s = "예약 주문 $count",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ScheduleItem(
    item: ScheduleOrderResultDetail = previewData(),
    onClick: () -> Unit = {},
    onRemove: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .padding(end = 12.dp)
        ) {
            val tint = if (item.disabled()) {
                MaterialTheme.colorScheme.surfaceDim
            } else {
                MaterialTheme.colorScheme.error
            }
            TouchIcon24(icon = Icons.Outlined.RemoveCircle, tint = tint) {
                if (!item.disabled()) onRemove()
            }

            val isBuy = item.sellBuyDivisionCode == "02"
            TrueText(
                s = if (isBuy) "매수" else "매도",
                fontSize = 12,
                color = if (isBuy) ChartColor.up else ChartColor.down,
                modifier = Modifier.weight(1f),
            )
            TrueText(
                s = item.nameKr,
                fontSize = 12,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(3f),
            )
            listOf(
                "${intFormatter.format(item.price.toDouble())}원" to 1.5f,
                "${intFormatter.format(item.orderReservedQuantity.toDouble())}주" to 1f,
            ).forEach { (s, w) ->
                TrueText(
                    s = s,
                    fontSize = 12,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    modifier = Modifier.weight(w),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
                .padding(end = 12.dp, bottom = 8.dp)
        ) {
            val endDate = "예약 종료: ${dateFormat(item.endDate)}" +
                    " | 처리 여부: ${item.processResult}"
            TrueText(
                s = endDate,
                fontSize = 12,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.End,
            )
        }
    }
}

private fun previewData() =
    ScheduleOrderResultDetail(
        seq = "",
        orderDate = "",
        receivedDate = "",
        code = "343300",
        orderDivisionCode = "",
        orderReservedQuantity = "",
        totalClearedQuantity = "",
        cancelOrderDate = "",
        orderTime = "",
        rejectReason = "",
        orderNumber = "",
        receivedTime = "",
        nameKr = "삼성전자",
        sellBuyDivisionCode = "",
        price = "50,000",
        totalClearedAmount = "",
        cancelReceivedTime = "",
        processResult = "처리",
        orderDivisionName = "",
        endDate = "2999.12.12",
    )
