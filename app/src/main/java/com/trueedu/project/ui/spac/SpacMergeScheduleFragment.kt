package com.trueedu.project.ui.spac

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.spac.MergeSchedule
import com.trueedu.project.repository.etc.readMergeSchedules
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
import java.net.URLEncoder
import javax.inject.Inject

/**
 * 더보기 > 스팩 합병 일정
 */
@AndroidEntryPoint
class SpacMergeScheduleFragment : BaseFragment() {
    companion object {
        fun show(fragmentManager: FragmentManager): SpacMergeScheduleFragment {
            val fragment = SpacMergeScheduleFragment()
            fragment.show(fragmentManager, "spac-merge-schedule")
            return fragment
        }
    }

    @Inject
    lateinit var spacManager: SpacManager

    private val loading = mutableStateOf(true)
    private val schedules = mutableStateOf<List<MergeSchedule>>(emptyList())
    private val infoVisible = mutableStateOf(false)

    override fun init() {
        super.init()

        lifecycleScope.launch {
            schedules.value = readMergeSchedules()
            loading.value = false
        }
    }

    @Composable
    override fun BodyScreen() {
        MergeScheduleScreen(
            loading = loading.value,
            schedules = schedules.value,
            showInfo = infoVisible.value,
            onShowInfo = { infoVisible.value = true },
            onDismissInfo = { infoVisible.value = false },
            onBack = ::dismissAllowingStateLoss,
            onClickItem = ::gotoStockDetail,
            openUrl = ::openUrl,
        )
    }

    private fun gotoStockDetail(schedule: MergeSchedule) {
        spacManager.spacList.value.firstOrNull { it.code == schedule.code }?.let {
            trueAnalytics.log(
                "${screenName()}__stock_detail__click",
                mapOf("nameKr" to it.nameKr),
            )
            StockDetailFragment.show(it, parentFragmentManager)
        }
    }

    private fun openUrl(url: String) {
        requireActivity().startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}

@Composable
private fun MergeScheduleScreen(
    loading: Boolean,
    schedules: List<MergeSchedule>,
    showInfo: Boolean,
    onShowInfo: () -> Unit,
    onDismissInfo: () -> Unit,
    onBack: () -> Unit,
    onClickItem: (MergeSchedule) -> Unit,
    openUrl: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            BackTitleTopBar(
                title = "스팩 합병 일정",
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
                schedules.isEmpty() -> EmptyMessage("합병 일정이 없습니다")
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(schedules, key = { it.code }) { schedule ->
                        MergeScheduleItem(
                            schedule = schedule,
                            onClick = { onClickItem(schedule) },
                            openUrl = openUrl,
                        )
                    }
                }
            }
        }
    }

    if (showInfo) {
        TrueDialog(
            popupType = PopupType.OK,
            title = "주의사항",
            desc = "※ 합병 일정은 변동될 수 있으며 실제 증권사 스케줄과 다소 차이가 날 수 있으니 공시 및 증권사 공지를 함께 확인하시기 바랍니다.",
            actions = listOf(ButtonAction("확인") { onDismissInfo() }),
            onDismiss = onDismissInfo,
        )
    }
}

private fun formatDateOrDash(s: String): String = if (s.isBlank()) "-" else dateFormat(s)

internal fun formatDateRangeOrDash(start: String, end: String): String {
    if (start.isBlank() && end.isBlank()) return "-"
    return "${formatDateOrDash(start)} ~ ${formatDateOrDash(end)}"
}

internal fun dartCompanySearchUrl(companyName: String): String {
    val encoded = URLEncoder.encode(companyName, Charsets.UTF_8.name())
    return "https://dart.fss.or.kr/dsab001/main.do?autoSearch=true&textCrpNM=$encoded"
}

@Composable
private fun InfoLine(text: String) {
    Spacer(modifier = Modifier.height(2.dp))
    TrueText(
        s = text,
        fontSize = 14,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = Int.MAX_VALUE,
    )
}

@Composable
private fun MergeScheduleItem(
    schedule: MergeSchedule,
    onClick: () -> Unit,
    openUrl: (String) -> Unit,
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

                val target = schedule.target.trim()
                if (target.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TrueText(
                            s = "합병 대상: ",
                            fontSize = 14,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = Int.MAX_VALUE,
                        )
                        TrueText(
                            s = target,
                            fontSize = 14,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { openUrl(dartCompanySearchUrl(target)) },
                            maxLines = Int.MAX_VALUE,
                            style = TextStyle(textDecoration = TextDecoration.Underline),
                        )
                    }
                } else {
                    TrueText(
                        s = "합병 대상: -",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = Int.MAX_VALUE,
                    )
                }
                InfoLine("합병반대의사통지 접수기간: ${formatDateRangeOrDash(schedule.dissentNoticeStartDate, schedule.dissentNoticeEndDate)}")
                InfoLine("주식매수청구권 행사기간: ${formatDateRangeOrDash(schedule.appraisalRightStartDate, schedule.appraisalRightEndDate)}")
                InfoLine("매매거래 정지예정기간: ${formatDateRangeOrDash(schedule.tradingHaltStartDate, schedule.tradingHaltEndDate)}")
                InfoLine("신주의 상장예정일: ${formatDateOrDash(schedule.newShareListingDate)}")

                Spacer(modifier = Modifier.height(8.dp))

                val disclosureUrl = schedule.disclosureUrl.trim()
                if (disclosureUrl.isNotEmpty()) {
                    TrueText(
                        s = "전자 공시 보기",
                        fontSize = 14,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { openUrl(disclosureUrl) },
                        maxLines = Int.MAX_VALUE,
                        style = TextStyle(textDecoration = TextDecoration.Underline),
                    )
                } else {
                    TrueText(
                        s = "전자 공시 보기: -",
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = Int.MAX_VALUE,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MergeScheduleItemPreview() {
    MergeScheduleItem(
        schedule = MergeSchedule(
            nameKr = "하나31호스팩",
            code = "469900",
            target = "예시컴퍼니",
            dissentNoticeStartDate = "20261101",
            dissentNoticeEndDate = "20261115",
            appraisalRightStartDate = "20261116",
            appraisalRightEndDate = "20261205",
            tradingHaltStartDate = "20261210",
            tradingHaltEndDate = "20261228",
            newShareListingDate = "20261229",
            disclosureUrl = "https://dart.fss.or.kr",
        ),
        onClick = {},
        openUrl = {},
    )
}
