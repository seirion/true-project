package com.trueedu.project.ui.views.spac

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.firebase.UserAsset
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.common.DesignatedBadge
import com.trueedu.project.ui.views.common.HaltBadge
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.home.EmptyHome
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.rateFormatter

class SpacScreenOld(
    private val vm: SpacViewModelOld,
    private val trueAnalytics: TrueAnalytics,
    private val fragmentManager: FragmentManager,
): BottomNavScreen {
    companion object {
        private val TAG = SpacScreenOld::class.java.simpleName
    }

    @Composable
    override fun Draw() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "보유 스팩 종목",
                    onBack = null,
                    actionIcon = Icons.Outlined.Search,
                    onAction = {
                        trueAnalytics.clickButton("${screenName()}__search__click")
                        SpacListFragment.show(fragmentManager)
                    }
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

            if (vm.manualAssets.assets.value.isEmpty()) {
                EmptyHome()
                return@Scaffold
            }

            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                contentPadding = PaddingValues(top = 8.dp, bottom = 56.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                item { TotalAssetView(vm.manualAssets.assets.value, vm.totalValues.value) }
                itemsIndexed(vm.manualAssets.assets.value, { _, item -> item.code}) { _, item ->
                    val stock = vm.stockPool.get(item.code) ?: return@itemsIndexed

                    // 현재 가격이 없으면 전일 가격으로 표시함
                    val currentPrice = vm.priceMap[item.code] ?: stock.prevPrice()?.toDouble()
                    SpacAssetItem(
                        item = item,
                        stock = stock,
                        currentPrice = currentPrice,
                        onItemClick = {
                            trueAnalytics.clickButton("${screenName()}__item__click")
                            StockDetailFragment.show(stock, fragmentManager)
                        },
                        onPriceClick = { onPriceClick(item.code) }
                    )
                }
            }
        }
    }

    override fun onStart() {
        vm.onStart()
    }

    override fun onStop() {
        vm.onStop()
    }

    private fun onPriceClick(code: String) {
        trueAnalytics.clickButton("${screenName()}__price__click")
        if (vm.tokenKeyManager.userKey.value != null) {
            OrderFragment.show(code, fragmentManager)
        }
    }
}

@Composable
fun TotalAssetView(assets: List<UserAsset>, spacProfit: SpacProfit) {
    val total = assets.sumOf { it.price * it.quantity }
    val totalString = intFormatter.format(total)

    TrueText(
        s = totalString,
        fontSize = 24,
        fontWeight = FontWeight.W500,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    )

    if (spacProfit.totalCost == 0.0) return

    val profit = spacProfit.totalValue - spacProfit.totalCost
    val profitString = intFormatter.format(profit)

    val rate = profit / spacProfit.totalCost * 100
    val profitRateString = rateFormatter.format(rate, true)

    // 수익/수익률
    TrueText(
        s = "$profitString ($profitRateString)",
        fontSize = 14,
        color = ChartColor.color(profit),
        modifier = Modifier.padding(horizontal = 8.dp)
    )
}

@Composable
fun SpacAssetItem(
    item: UserAsset,
    stock: StockInfo?,
    currentPrice: Double?,
    onItemClick: (String) -> Unit,
    onPriceClick: (String) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.code) }
            .padding(8.dp)
    ) {
        Column {
            Row {
                TrueText(
                    s = item.nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                if (stock?.halt() == true) {
                    Margin(2)
                    HaltBadge()
                }
                if (stock?.designated() == true) {
                    Margin(2)
                    DesignatedBadge()
                }
            }

            val priceString = intFormatter.format(item.price)
            val quantityString = intFormatter.format(item.quantity) // 정수만
            val subText = "${priceString}원 • ${quantityString}주" // 수량
            TrueText(
                s = subText,
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable { onPriceClick(item.code) }
        ) {
            val totalValue = item.price * item.quantity
            val totalValueString = intFormatter.format(totalValue)
            val profit = if (currentPrice == null) {
                0.0
            } else {
                (currentPrice - item.price) * item.quantity
            }
            val profitString = intFormatter.format(profit)
            val profitRate = if (currentPrice == null || item.price == 0.0) {
                0.0
            } else {
                (currentPrice - item.price) / item.price * 100
            }
            val profitRateString = rateFormatter.format(profitRate, true)
            TrueText(
                s = totalValueString,
                fontSize = 14,
                fontWeight = FontWeight.W600,
                color = ChartColor.color(profit),
            )
            // 실시간 가격이 있을 때만 표시하기
            if (currentPrice != null) {
                TrueText(
                    s = "$profitString ($profitRateString)",
                    fontSize = 12,
                    color = ChartColor.color(profit),
                )
            }
        }
    }
}
