package com.trueedu.project.ui.views.spac

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.firebase.StockInfoKospi
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomSelectionFragment
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.common.DesignatedBadge
import com.trueedu.project.ui.views.common.HaltBadge
import com.trueedu.project.ui.views.common.HoldingBadge
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.utils.formatter.dateFormat
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.rateFormatter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SpacListFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SpacListFragment {
            val fragment = SpacListFragment()
            fragment.show(fragmentManager, "spac")
            return fragment
        }
    }

    private val vm by viewModels<SpacListViewModel>()

    @Inject
    lateinit var remoteConfig: RemoteConfig
    @Inject
    lateinit var admobManager: AdmobManager
    @Inject
    lateinit var spacManager: SpacManager

    private fun onPriceClick(code: String) {
        trueAnalytics.clickButton("${screenName()}__price__click")
        if (vm.hasAppKey()) {
            OrderFragment.show(code, parentFragmentManager)
        } else {
            AppKeyInputFragment.show(false, parentFragmentManager)
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "스팩 검색",
                    onBack = ::dismissAllowingStateLoss,
                    actionIcon = Icons.Outlined.ViewList,
                    onAction = ::onSortOption,
                )
            },
            bottomBar = {
                if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                    NativeAdView(admobManager.nativeAd.value!!)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val loading by spacManager.loading.collectAsState()
            if (loading) {
                LoadingView()
                return@Scaffold
            }

            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                item { SearchBar(searchText = vm.searchInput) {} }
                stickyHeader { SpacSectionView() }

                itemsIndexed(vm.stocks.value, key = { i, _ -> i }) { i, item ->
                    val redemptionValue = spacManager.redemptionValueMap[item.code]
                    val expectedProfit = redemptionValue?.first
                    val expectedProfitRate = redemptionValue?.second
                    SpacItem(i, item,
                        spacManager.priceMap[item.code] ?: 0.0,
                        spacManager.priceChangeMap[item.code],
                        spacManager.volumeMap[item.code] ?: 0L,
                        expectedProfit,
                        expectedProfitRate,
                        vm.holdingNum(item.code),
                        ::onPriceClick
                    ) {
                        StockDetailFragment.show(item, parentFragmentManager)
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        spacManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        spacManager.onStop()
    }

    // 정렬하기
    private fun onSortOption() {
        val selected = SpacSort.entries.indexOfFirst { it == vm.sort.value }
        BottomSelectionFragment.show(
            selected = selected,
            title = "정렬 방법",
            list = SpacSort.entries.map { it.title },
            onSelected = {
                val option = SpacSort.entries[it]
                trueAnalytics.clickButton(
                    "${screenName()}__sort__click",
                    mapOf("sort_type" to option.title)
                )
                vm.setSort(option)
            },
            fragmentManager = parentFragmentManager,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SpacItem(
    index: Int = 1,
    item: StockInfo = StockInfoKospi("003456", "삼성전자", ""),
    price: Double = 2000.0,
    priceChange: Double? = 10.0,
    volume: Long = 1234L,
    expectedProfit: Int? = null, // 청산 시 기대 수익
    expectedProfitRate: Double? = null, // 청산 시 기대 수익률(%)
    holdingNum: Double = 1.0,
    onPriceClick: (String) -> Unit = {},
    onClick: () -> Unit = {},
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Column {
            Row {
                TrueText(
                    s = item.nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )

                if (holdingNum > 0) {
                    Margin(2)
                    val s = intFormatter.format(holdingNum)
                    HoldingBadge(s)
                }
                if (item.halt()) {
                    Margin(2)
                    HaltBadge()
                }
                if (item.designated()) {
                    Margin(2)
                    DesignatedBadge()
                }
            }
            val listingDateStr = dateFormat(item.listingDate() ?: "")
            val marketCapStr = "${item.marketCap()}억"
            TrueText(
                s = dateFormat("$listingDateStr • $marketCapStr"),
                fontSize = 10,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(1f)
            ) {
                val volumeString = intFormatter.format(volume)

                TrueText(
                    s = volumeString,
                    fontSize = 13,
                    color = MaterialTheme.colorScheme.secondary,
                )
                val redemptionPriceString =
                    if (expectedProfit != null && expectedProfitRate != null) {
                        val rateString = rateFormatter.format(expectedProfitRate, true)
                        "${intFormatter.format(expectedProfit)} (${rateString})"
                    } else {
                        "-"
                    }
                TrueText(
                    s = redemptionPriceString,
                    fontSize = 12,
                    color = ChartColor.color(expectedProfitRate ?: 0.0),
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .width(60.dp)
                    .clickable { onPriceClick(item.code) }
            ) {
                val priceString = intFormatter.format(price)
                TrueText(
                    s = priceString,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                )
                val priceChangeString = if (priceChange == null) {
                    "-"
                } else {
                    intFormatter.format(priceChange, true)
                }
                TrueText(
                    s = priceChangeString,
                    fontSize = 12,
                    color = ChartColor.color(priceChange ?: 0.0)
                )
            }
        }
    }
}

@Composable
fun SpacSectionView() {
    val textColor = MaterialTheme.colorScheme.secondary

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceDim
            )
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            TrueText(s = "종목", fontSize = 12, color = textColor)
            TrueText(s = "상장일 • 시가총액", fontSize = 12, color = textColor)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(end = 4.dp)
                    .weight(1f)
            ) {
                TrueText(s = "거래량", fontSize = 12, color = textColor)
                TrueText(s = "청산가(수익)", fontSize = 12, color = textColor)
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.width(60.dp)
                    .padding(end = 8.dp)
            ) {
                TrueText(s = "가격", fontSize = 12, color = textColor)
            }
        }
    }
}
