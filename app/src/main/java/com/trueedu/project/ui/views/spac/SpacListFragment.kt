package com.trueedu.project.ui.views.spac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.firebase.StockInfoKospi
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomSelectionFragment
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.common.DesignatedBadge
import com.trueedu.project.ui.views.common.HaltBadge
import com.trueedu.project.ui.views.common.HoldingBadge
import com.trueedu.project.ui.views.order.OrderFragment
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

    private fun onPriceClick(code: String) {
        trueAnalytics.clickButton("${screenName()}__price__click")
        if (vm.hasAppKey()) {
            OrderFragment.show(code, parentFragmentManager)
        } else {
            AppKeyInputFragment.show(false, parentFragmentManager)
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "스팩 종목",
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(vm.stocks.value, key = { _, item -> item.code }) { i, item ->
                        SpacItem(i, item, vm.priceMap[item.code],
                            vm.redemptionValueMap[item.code]?.first,
                            vm.redemptionValueMap[item.code]?.second,
                            vm.hasStock(item.code),
                            ::onPriceClick) {
                            StockDetailFragment.show(item, parentFragmentManager)
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        vm.onStart()
    }

    override fun onStop() {
        super.onStop()
        vm.onStop()
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
private fun SpacItem(
    index: Int = 1,
    item: StockInfo = StockInfoKospi("003456", "삼성전자", ""),
    currentPrice: Double? = null,
    expectedProfit: Int? = null, // 청산 시 기대 수익
    expectedProfitRate: Double? = null, // 청산 시 기대 수익률(%)
    hasThisStock: Boolean = true,
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

                if (hasThisStock) {
                    Margin(2)
                    HoldingBadge()
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

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable { onPriceClick(item.code) }
        ) {
            val price = currentPrice
                ?: item.prevPrice()?.toDouble() // 전일 종가
                ?: 0.0
            val priceString = intFormatter.format(price)
            TrueText(
                s = priceString,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )

            val redemptionPriceString = if (expectedProfit != null && expectedProfitRate != null) {
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
    }
}
