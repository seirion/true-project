package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.StockInfoKospi
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.views.search.SearchList
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockDetailFragment: BaseFragment() {
    companion object {
        fun show(
            stockInfo: StockInfo,
            fragmentManager: FragmentManager
        ): StockDetailFragment {
            val fragment = StockDetailFragment()
            fragment.stockInfo = stockInfo
            fragment.show(fragmentManager, "stock-detail")
            return fragment
        }
    }

    lateinit var stockInfo: StockInfo
    private val infoList = mutableStateOf<List<Pair<String, String?>>>(emptyList())

    override fun init() {
        if (stockInfo is StockInfoKospi) {
            val stockInfo = stockInfo as StockInfoKospi
            infoList.value = listOf(
                "거래정지" to stockInfo.halt(),
                "관리종목" to stockInfo.designated(),
                "상장일자" to stockInfo.listingDate(),
                "상장주수" to stockInfo.listingShares(),
                "공매도과열" to stockInfo.shortSellingOverheating(),
                "이상급등" to stockInfo.unusualPriceSurge(),
                "매출액" to stockInfo.sales(),
                "영업이익" to stockInfo.operatingProfit(),
                "시가총액" to stockInfo.marketCap(),
                "기준가" to stockInfo.prevPrice(),
                "전일거래량" to stockInfo.prevVolume(),
            )
        } else {
            // TODO
        }
    }

    @Composable
    override fun BodyScreen() {
        if (!::stockInfo.isInitialized) dismissAllowingStateLoss()
        Scaffold(
            topBar = { BackTitleTopBar(stockInfo.nameKr, ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                infoList.value.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        BasicText(s = it.first, fontSize = 16, color = MaterialTheme.colorScheme.primary)
                        BasicText(s = it.second ?: "", fontSize = 16, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
