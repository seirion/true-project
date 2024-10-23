package com.trueedu.project.ui.views

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.StockInfoKosdaq
import com.trueedu.project.model.dto.StockInfoKospi
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockPool: StockPool,
    val priceRemote: PriceRemote,
    val priceManager: RealPriceManager,
): ViewModel() {

    companion object {
        private val TAG = StockDetailViewModel::class.java.simpleName
    }

    private lateinit var stockInfo: StockInfo
    val infoList = mutableStateOf<List<Pair<String, String?>>>(emptyList())

    // 가격 정보 (api)
    val basePrice = mutableStateOf<PriceResponse?>(null)

    fun init(stockInfo: StockInfo) {
        this.stockInfo = stockInfo
        initInfoList()
        priceManager.pushRequest(
            stockInfo.code,
            listOf(stockInfo.code)
        )

        priceRemote.currentPrice(stockInfo.code)
            .onEach {
                basePrice.value = it
            }
            .launchIn(viewModelScope)
    }

    fun destroy() {
        priceManager.popRequest(stockInfo.code)
    }

    private fun initInfoList() {
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
            val stockInfo = stockInfo as StockInfoKosdaq
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
        }
    }
}
