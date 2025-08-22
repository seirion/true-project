package com.trueedu.project.admin.spac

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastDistinctBy
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.firebase.SpacStatus
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.safeLong
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpacAdminFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SpacAdminFragment {
            return SpacAdminFragment().also {
                it.show(fragmentManager, "spac_admin")
            }
        }

        private val statusList = listOf(
            "", "합병심사", "합병승인", "반대의사", "매수청구", "상장폐지"
        )
    }

    @Inject
    lateinit var stockPool: StockPool
    @Inject
    lateinit var spacManager: SpacManager
    @Inject
    lateinit var spacStatusManager: SpacStatusManager

    private val loading = mutableStateOf(false)
    private val namePrices = mutableStateOf<List<SpacStatus>>(emptyList())

    override fun init() {
        lifecycleScope.launch {
            launch {
                stockPool.status
                    .collect {
                        if (it == StockPool.Status.SUCCESS) {
                            loading.value = false
                        }
                    }
            }

            val oldValues = spacStatusManager.load()
                .filter { s ->
                    // 상폐 되어 검색되지 않는 종목은 제외
                    stockPool.search { it.nameKr == s.nameKr }.firstOrNull() != null
                }
            val newValues = spacRedemptionPrices
                .map { it.split("\t") }
                .mapNotNull { spac ->
                    val nameKr = spac.first().takeWhile { !it.isWhitespace() }
                    val stock = stockPool.search { it.nameKr == nameKr }.firstOrNull()
                        ?: return@mapNotNull null
                    val price = spac.last().toInt()
                    SpacStatus(
                        code = stock.code,
                        nameKr = nameKr,
                        redemptionPrice = price,
                        status = "",
                    )
                }

            // fastDistinctBy 알고리즘에 따라 newValues 를 우선하여 사용함
            namePrices.value = (newValues + oldValues)
                .fastDistinctBy(SpacStatus::code)
                .sortedBy {
                    val s = stockPool.get(it.code)
                    s!!.listingDate() ?: ""
                }
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "스팩 청산 가격",
                    onBack = ::dismissAllowingStateLoss,
                )
            },
            bottomBar = {
                BottomBar("저장", true, ::onSave)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (loading.value) {
                    item { LoadingView() }
                    return@LazyColumn
                }
                itemsIndexed(namePrices.value, key = { _, item -> item.code }) { i, item ->
                    val stock = stockPool.get(item.code)
                    val nameKr = item.nameKr
                    val code = item.code
                    val currentPrice = stock?.prevPrice()?.toDouble() ?: 0.0
                    SpacItemInternal(nameKr, code, currentPrice, item.redemptionPrice!!, item.status) {
                        val index = statusList.indexOf(item.status)
                        val currentStatus = statusList[(index + 1) % statusList.size]
                        namePrices.value = namePrices.value.map {
                            if (it.code == code) {
                                it.copy(status = currentStatus)
                            } else {
                                it
                            }
                        }
                    }
                }
            }
        }
    }

    private fun beforeTax(p: Int, parValue: Long): Int {
        val base = if (parValue != 100L) 10_000 else 2_000
        return ((p - base) / 0.846).toInt() + base
    }

    private fun onSave() {
        val list = namePrices.value
        MainScope().launch {
            spacStatusManager.write(
                list,
                {
                    spacManager.spacStatusMap.value = list.associateBy { it.code }
                    MainScope().launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "저장 완료", Toast.LENGTH_SHORT).show()
                    }
                },

                {
                    MainScope().launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "저장 실패", Toast.LENGTH_SHORT).show()
                    }
                },
            )
        }
    }
}

@Composable
private fun SpacItemInternal(
    nameKr: String,
    code: String,
    currentPrice: Double,
    spacRedemptionPrices: Int,
    status: String,
    onClick: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Column {
            TrueText(
                s = nameKr,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )
            TrueText(
                s = code,
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TrueText(
                s = status,
                fontSize = 14,
                textAlign = TextAlign.End,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.widthIn(min = 120.dp)
                    .background(color = MaterialTheme.colorScheme.secondaryContainer)
                    .clickable { onClick() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Margin(12)
            val color = ChartColor.color(spacRedemptionPrices - currentPrice)
            Column(
                modifier = Modifier.width(60.dp),
                horizontalAlignment = Alignment.End,
            ) {
                TrueText(
                    s = intFormatter.format(currentPrice),
                    fontSize = 13,
                    color = MaterialTheme.colorScheme.primary,
                )
                TrueText(
                    s = intFormatter.format(spacRedemptionPrices),
                    fontSize = 14,
                    color = color,
                )
            }
        }
    }
}
