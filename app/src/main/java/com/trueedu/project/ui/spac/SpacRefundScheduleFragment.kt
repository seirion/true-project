package com.trueedu.project.ui.spac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.spac.RefundSchedule
import com.trueedu.project.repository.etc.readRefundSchedules
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.ButtonAction
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.PopupType
import com.trueedu.project.ui.common.TrueDialog
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.utils.formatter.dateFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 더보기 > 스팩 청산 일정
 */
@AndroidEntryPoint
class SpacRefundScheduleFragment : BaseFragment() {
    companion object {
        fun show(fragmentManager: FragmentManager): SpacRefundScheduleFragment {
            val fragment = SpacRefundScheduleFragment()
            fragment.show(fragmentManager, "spac-refund-schedule")
            return fragment
        }
    }

    @Inject
    lateinit var spacManager: SpacManager

    private val loading = mutableStateOf(true)
    private val schedules = mutableStateOf<List<RefundSchedule>>(emptyList())
    private val infoVisible = mutableStateOf(false)

    override fun init() {
        super.init()

        lifecycleScope.launch {
            schedules.value = readRefundSchedules()
            loading.value = false
        }
    }

    @Composable
    override fun BodyScreen() {
        RefundScheduleScreen(
            loading = loading.value,
            schedules = schedules.value,
            showInfo = infoVisible.value,
            onShowInfo = { infoVisible.value = true },
            onDismissInfo = { infoVisible.value = false },
            onBack = ::dismissAllowingStateLoss,
            onClickItem = ::gotoStockDetail,
        )
    }

    private fun gotoStockDetail(schedule: RefundSchedule) {
        spacManager.spacList.value.firstOrNull { it.code == schedule.code }?.let {
            trueAnalytics.log(
                "${screenName()}__stock_detail__click",
                mapOf("nameKr" to it.nameKr),
            )
            StockDetailFragment.show(it, parentFragmentManager)
        }
    }
}

@Composable
private fun RefundScheduleScreen(
    loading: Boolean,
    schedules: List<RefundSchedule>,
    showInfo: Boolean,
    onShowInfo: () -> Unit,
    onDismissInfo: () -> Unit,
    onBack: () -> Unit,
    onClickItem: (RefundSchedule) -> Unit,
) {
    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "스팩 청산 일정",
                onBack = onBack,
                actionIcon = Icons.Filled.Info,
                onAction = onShowInfo,
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background),
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                loading -> LoadingView()
                schedules.isEmpty() -> EmptyMessage("청산 일정이 없습니다")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(schedules, key = { it.code }) { schedule ->
                        RefundScheduleItem(schedule, onClick = { onClickItem(schedule) })
                    }
                }
            }
        }
    }

    if (showInfo) {
        TrueDialog(
            popupType = PopupType.OK,
            title = "주의사항",
            desc = "※ 청산 가격은 오차가 발생할 수 있으므로 증권사에서 정확한 값을 확인하시기 바랍니다.",
            actions = listOf(ButtonAction("확인") { onDismissInfo() }),
            onDismiss = onDismissInfo,
        )
    }
}

@Composable
internal fun EmptyMessage(text: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        TrueText(
            s = text,
            fontSize = 16,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RefundScheduleItem(
    schedule: RefundSchedule,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TrueText(
                    s = "${schedule.nameKr} (${schedule.code})",
                    fontSize = 16,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                TrueText(
                    s = "입금일: ${dateFormat(schedule.date)}",
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                schedule.refundAmount?.let { amount ->
                    Spacer(modifier = Modifier.height(2.dp))
                    TrueText(
                        s = "분배금: ${refundAmountText(amount)}${if (schedule.fixed) " (확정)" else " (예상)"}",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

/** 분배금은 소수 첫째 자리까지 필요한 경우만 표시 (예: 2,172.4 / 2,150) */
internal fun refundAmountText(amount: Double): String =
    java.text.DecimalFormat("#,##0.##").format(amount)

@Preview(showBackground = true)
@Composable
private fun RefundScheduleItemPreview() {
    RefundScheduleItem(
        RefundSchedule(
            nameKr = "교보15호스팩",
            code = "465320",
            date = "20261221",
            refundAmount = 2172.4,
            fixed = false,
        ),
        onClick = {},
    )
}
