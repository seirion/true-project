package com.trueedu.project.ui.views.trading

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealOrderManager
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.model.dto.price.TradeResponse
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    val stockPool: StockPool,
    val priceRemote: PriceRemote,
    val priceManager: RealPriceManager,
    val orderManager: RealOrderManager,
): ViewModel() {

    companion object {
        private val TAG = TradingViewModel::class.java.simpleName
    }

    private var code: String = ""

    // api 응답
    private val trade = mutableStateOf<TradeResponse?>(null)
    private val basePrice = mutableStateOf<PriceResponse?>(null)

    fun init(code: String) {
        this.code = code
        priceManager.pushRequest(
            code,
            listOf(code)
        )
        orderManager.beginRequests(code)

        // 호가 기본값
        priceRemote.currentTrade(code)
            .onEach {
                Log.d(TAG, "호가 api: $it")
                trade.value = it
            }
            .launchIn(viewModelScope)

        // 가격 기본값
        priceRemote.currentPrice(code)
            .onEach {
                basePrice.value = it
            }
            .launchIn(viewModelScope)
    }

    fun destroy() {
        priceManager.popRequest(code)
        orderManager.stop()
    }

    fun stockInfo(): StockInfo? {
        return stockPool.get(code)
    }

    fun realTimeTrade(): RealTimeTrade? {
        val stockInfo = stockInfo() ?: return null
        return priceManager.dataMap[stockInfo.code]
    }

    fun price(): Double {
        return realTimeTrade()?.price
            ?: trade.value?.output2?.price?.toDouble()
            ?: basePrice.value?.output?.price?.toDouble()
            ?: 0.0
    }

    fun priceChange(): Double {
        return realTimeTrade()?.delta
            ?: basePrice.value?.output?.priceChange?.toDouble()
            ?: trade.value?.output2?.anticipatedPriceChange?.toDouble()
            ?: 0.0
    }

    fun priceChangeRate(): Double {
        return realTimeTrade()?.rate
            ?: basePrice.value?.output?.priceChangeRate?.toDouble()
            ?: trade.value?.output2?.anticipatedPriceChangeRate?.toDouble()
            ?: 0.0
    }
}
