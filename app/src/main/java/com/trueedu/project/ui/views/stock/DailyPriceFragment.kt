package com.trueedu.project.ui.views.stock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.price.StockDetail
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.views.common.TopStockInfoView
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

                item { TopStockInfoViewInternal(vm.dailyPrices.value!!.stockDetail) }
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
private fun TopStockInfoViewInternal(
    stockDetail: StockDetail
) {
    TopStockInfoView(
        price = stockDetail.price.toDouble(),
        previousPrice = stockDetail.previousPrice.toDouble(),
        priceChange = stockDetail.priceChange.toDouble(),
        priceChangeRate = stockDetail.priceChangeRate.toDouble(),
        volume = stockDetail.volume.toDouble(),
        `open` = stockDetail.open.toDouble(),
        high = stockDetail.high.toDouble(),
        low = stockDetail.low.toDouble(),
    )
}