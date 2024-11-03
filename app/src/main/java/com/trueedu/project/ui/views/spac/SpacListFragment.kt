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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.StockInfoKospi
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.views.common.Badge
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.utils.formatter.cashFormatter
import dagger.hilt.android.AndroidEntryPoint

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
            topBar = { BackTitleTopBar("스팩 종목", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                //SearchBar(searchText = "") {}
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(vm.stocks.value, key = { _, item -> item.code }) { i, item ->
                        SpacItem(i, item, ::onPriceClick) {

                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpacItem(
    index: Int = 1,
    item: StockInfo = StockInfoKospi("003456", "삼성전자", ""),
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

                if (item.halt()) {
                    Margin(2)
                    Badge("정", MaterialTheme.colorScheme.error)
                }
                if (item.designated()) {
                    Margin(2)
                    Badge("관", Color(0xFFF57C00))
                }
            }
            TrueText(
                s = item.listingDate() ?: "",
                fontSize = 10,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable { onPriceClick(item.code) }
        ) {
            val price = item.prevPrice() // 전일 종가
            val priceString = cashFormatter.format(price?.toDouble() ?: 0.0)
            TrueText(
                s = priceString,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )
            TrueText(
                s = "${item.marketCap()}억",
                fontSize = 10,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
