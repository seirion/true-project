package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.ManualAssets
import com.trueedu.project.data.realtime.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.model.dto.firebase.SpacStatus
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.utils.formatter.dateFormat
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.numberFormatString
import com.trueedu.project.utils.formatter.safeDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StockDetailViewModel @Inject constructor(
    private val stockPool: StockPool,
    private val spacStatusManager: SpacStatusManager,
    private val priceRemote: PriceRemote,
    val priceManager: RealPriceManager,
    private val tokenKeyManager: TokenKeyManager,
    private val assets: ManualAssets,
): ViewModel() {

    companion object {
        private val TAG = StockDetailViewModel::class.java.simpleName
    }

    private lateinit var stockInfo: StockInfo
    val infoList = mutableStateOf<List<Pair<String, String?>>>(emptyList())

    // 가격 정보 (api)
    val basePrice = mutableStateOf<PriceResponse?>(null)

    val spacStatus = mutableStateOf<SpacStatus?>(null)

    fun init(stockInfo: StockInfo) {
        this.stockInfo = stockInfo
        initInfoList()
        priceManager.pushRequest(
            stockInfo.code,
            listOf(stockInfo.code)
        )

        if (stockInfo.spac()) {
            viewModelScope.launch {
                val list = spacStatusManager.load()
                val spacStatus = list.firstOrNull { it.code == stockInfo.code }
                withContext(Dispatchers.Main) {
                    this@StockDetailViewModel.spacStatus.value = spacStatus
                }
            }
        }

        if (tokenKeyManager.userKey.value != null) {
            priceRemote.currentPrice(stockInfo.code)
                .onEach {
                    basePrice.value = it
                }
                .catch {
                    Log.d(TAG, "가격 데이터 받기 실패: $it")
                }
                .launchIn(viewModelScope)
        }
    }

    fun destroy() {
        priceManager.popRequest(stockInfo.code)
    }

    fun initInfoList() {
        infoList.value = listOf(
            "전일가격" to numberFormatString(stockInfo.prevPrice()) + "원",
            "전일거래량" to numberFormatString(stockInfo.prevVolume()),
            "시가총액" to numberFormatString(stockInfo.marketCap()) + "억",
            "상장일자" to dateFormat(stockInfo.listingDate()),
            "상장주수" to numberFormatString(stockInfo.listingShares()) + "K",
            //"공매도과열" to stockInfo.shortSellingOverheating().toYnString(),
            //"이상급등" to stockInfo.unusualPriceSurge().toYnString(),
        ) + if (stockInfo.spac()) {
            assets.get(stockInfo.code)?.let {
                val priceQuantity = "${intFormatter.format(it.price)} • ${intFormatter.format(it.quantity)}주"
                val memo = if (it.memo.isNotEmpty()) "\n${it.memo}" else ""
                listOf("수동 입력 자산" to "${priceQuantity}${memo}")
            } ?: emptyList()
        } else {
            listOf(
                "매출액" to numberFormatString(stockInfo.sales()) + "억",
                "영업이익" to numberFormatString(stockInfo.operatingProfit()) + "억",
            )
        }
    }

    fun hasAppKey(): Boolean {
        return tokenKeyManager.userKey.value != null
    }

    fun currentPrice(): Double {
        val realTimeTrade = priceManager.dataMap[stockInfo.code]
        return realTimeTrade?.price
            ?: basePrice.value?.output?.price?.toDouble()
            ?: stockInfo.prevPrice().safeDouble()
    }
}
