package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.utils.formatter.dateFormat
import com.trueedu.project.utils.formatter.numberFormat
import com.trueedu.project.utils.formatter.toYnString
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockPool: StockPool,
    private val priceRemote: PriceRemote,
    val priceManager: RealPriceManager,
    private val tokenKeyManager: TokenKeyManager,
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
            .catch {
                Log.d(TAG, "가격 데이터 받기 실패: $it")
            }
            .launchIn(viewModelScope)
    }

    fun destroy() {
        priceManager.popRequest(stockInfo.code)
    }

    private fun initInfoList() {
        infoList.value = listOf(
            "상장일자" to dateFormat(stockInfo.listingDate()),
            "상장주수" to numberFormat(stockInfo.listingShares()) + "K",
            "매출액" to numberFormat(stockInfo.sales()) + "억",
            "영업이익" to numberFormat(stockInfo.operatingProfit()) + "억",
            "시가총액" to numberFormat(stockInfo.marketCap()) + "억",
            "기준가" to numberFormat(stockInfo.prevPrice()) + "원",
            "전일거래량" to numberFormat(stockInfo.prevVolume()),
            "공매도과열" to stockInfo.shortSellingOverheating().toYnString(),
            "이상급등" to stockInfo.unusualPriceSurge().toYnString(),
        )
    }

    fun hasAppKey(): Boolean {
        return tokenKeyManager.userKey.value != null
    }
}
