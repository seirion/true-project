package com.trueedu.project.ui.views.stock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.price.DailyPrice
import com.trueedu.project.model.dto.price.StockDetail
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.cashFormatter
import com.trueedu.project.utils.formatter.rateFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DailyPriceFragment: BaseFragment() {
    companion object {
        fun show(
            code: String,
            fragmentManager: FragmentManager
        ): DailyPriceFragment {
            val fragment = DailyPriceFragment()
            fragment.code = code
            fragment.show(fragmentManager, "daily_price")
            return fragment
        }
    }

    private lateinit var code: String
    private val vm by viewModels<DailyPriceViewModel>()

    override fun init() {
        vm.init(code)
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BodyScreen() {
        if (!::code.isInitialized) dismissAllowingStateLoss()

        Scaffold(
            topBar = {
                val nameKr = vm.dailyPrices.value?.stockDetail?.nameKr ?: ""
                BackTitleTopBar(nameKr, ::dismissAllowingStateLoss)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val scrollState = rememberLazyListState()
            LazyColumn (
                state = scrollState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (vm.dailyPrices.value == null) {
                    item { LoadingView() }
                    return@LazyColumn
                }

                item { StockInfoView(vm.dailyPrices.value!!.stockDetail) }
                stickyHeader { DailyPriceSection() }

                val items = vm.dailyPrices.value!!.dailyPrices
                itemsIndexed(items, key = { _, item -> item.date }) { index, item ->
                    val bgColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.surfaceDim
                    }
                    DailyPriceCell(item, bgColor)

                    if (index == items.lastIndex) {
                        vm.loadMore(code)
                    }
                }
            }
        }
    }
}

@Composable
private fun StockInfoView(stockDetail: StockDetail) {
    val priceString = cashFormatter.format(
        stockDetail.price.toDouble()
    )
    val priceChange = stockDetail.priceChange.toDouble()
    val priceChangeRate = stockDetail.priceChangeRate.toDouble()
    val volume = stockDetail.volume.toDouble()
    val textColor = ChartColor.color(priceChange)

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            //horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f),
        ) {
            // 현재 가격
            BasicText(
                s = priceString,
                fontSize = 24,
                fontWeight = FontWeight.W500,
                color = textColor,
            )
            // 전일 대비
            BasicText(
                s = "${cashFormatter.format(priceChange, false)} " +
                        "(${rateFormatter.format(priceChangeRate)})",
                fontSize = 12,
                color = ChartColor.color(priceChange)
            )
            // 거래량
            BasicText(
                s = "${cashFormatter.format(volume, false)}",
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Margin(16)
        Column(
            modifier = Modifier.weight(0.8f),
        ) {
            val basePrice = stockDetail.previousPrice.toDouble()
            listOf(
                "시가" to stockDetail.`open`.toDouble(),
                "고가" to stockDetail.high.toDouble(),
                "저가" to stockDetail.low.toDouble(),
            ).forEach { (title, value) ->
                val color = ChartColor.color(value - basePrice)
                val valueString = cashFormatter.format(value)
                PriceCellView(title, valueString, color)
            }
        }
    }
}
