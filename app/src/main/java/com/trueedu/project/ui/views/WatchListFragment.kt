package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull

@AndroidEntryPoint
class WatchListFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): WatchListFragment {
            val fragment = WatchListFragment()
            fragment.show(fragmentManager, "watch-list")
            return fragment
        }
    }

    private val vm by viewModels<WatchListViewModel>()

    @OptIn(ExperimentalFoundationApi::class)
    private var pagerState: PagerState? = null

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BodyScreen() {
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState?.currentPage }
                .mapNotNull { it?.mod(vm.pageCount()) }
                .collectLatest {
                    vm.currentPage.value = it
                    // 페이지가 바뀌면 실시간 요청 다시 하기
                    vm.requestRealtimePrice()
                }
        }
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "관심 종목 ${vm.currentPage.value ?: ""}",
                    onBack = ::dismissAllowingStateLoss,
                    actionIcon = Icons.Filled.Search,
                    onAction = ::onSearch
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            if (pagerState == null) {
                pagerState = rememberPagerState(
                    initialPage = 10000 * vm.pageCount(),
                    initialPageOffsetFraction = 0f,
                    pageCount = { 20000 * vm.pageCount() }, // infinite loop
                )
            }

            // 주식 정보와 관심 종목 정보를 모두 받아야 데이터 표시 가능
            if (
                vm.loading.value ||
                vm.stockPool.status.value != StockPool.Status.SUCCESS
            ) {
                LoadingView()
                return@Scaffold
            }

            HorizontalPager(
                state = pagerState!!,
                modifier = Modifier.fillMaxSize()
            ) { position ->
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val items = vm.getItems(position % vm.pageCount())
                    itemsIndexed(items, key = { _, item -> item }) { _, code ->
                        val stock = vm.getStock(code) ?: return@itemsIndexed
                        val tradeData = vm.priceManager.dataMap[code]
                        val price: Double = tradeData?.price ?: 0.0
                        val delta: Double = tradeData?.delta ?: 0.0
                        val rate: Double = tradeData?.rate ?: 0.0
                        val volume: Double = tradeData?.volume ?: 0.0

                        WatchingStockItem(
                            nameKr = stock.nameKr,
                            code = code,
                            price = price,
                            delta = delta,
                            rate = rate,
                            volume = volume,
                        )
                    }
                }
            }
        }
    }

    private fun onSearch() {
        trueAnalytics.clickButton("watch_list__search__click")
        StockSearchFragment.show(vm.currentPage.value, parentFragmentManager)
    }

    private fun gotoStockDetail(stockInfo: StockInfo) {
        StockDetailFragment.show(stockInfo, parentFragmentManager)
    }
}

@Composable
private fun WatchingStockItem(
    nameKr: String,
    code: String,
    price: Double,
    delta: Double,
    rate: Double,
    volume: Double,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            BasicText(
                s = nameKr,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
            BasicText(
                s = "(${code})",
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val totalValueString = formatter.format(price)
            BasicText(
                s = totalValueString,
                fontSize = 13,
                color = MaterialTheme.colorScheme.primary,
            )

            val profitString = formatter.format(delta, true)
            val profitRateString = rateFormatter.format(rate, true)
            BasicText(
                s = "$profitString ($profitRateString)",
                fontSize = 12,
                color = ChartColor.color(delta),
            )
        }
    }
}
