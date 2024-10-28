package com.trueedu.project.ui.views.order

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealOrderManager
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.model.dto.price.TradeResponse
import com.trueedu.project.model.ws.RealTimeOrder
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.OrderRemote
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val local: Local,
    val stockPool: StockPool,
    private val priceRemote: PriceRemote,
    private val orderRemote: OrderRemote,
    private val priceManager: RealPriceManager,
    private val orderManager: RealOrderManager,
): ViewModel() {

    companion object {
        private val TAG = OrderViewModel::class.java.simpleName

        private val empty = List(10) { 0.0 to 0.0 }
    }

    private var code: String = ""

    // api 응답
    private val tradeBase = mutableStateOf<TradeResponse?>(null)
    private val basePrice = mutableStateOf<PriceResponse?>(null)

    val realTimeQuotes = mutableStateOf<RealTimeOrder?>(null)

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
                tradeBase.value = it
            }
            .launchIn(viewModelScope)

        // 가격 기본값
        priceRemote.currentPrice(code)
            .onEach {
                basePrice.value = it
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            snapshotFlow { orderManager.data.value }
                .filterNotNull()
                .filter { it.code == this@OrderViewModel.code }
                .collect {
                    val stock = stockPool.get(it.code)
                    Log.d(TAG, "실시간 호가: ${it.code} ${stock?.nameKr}")
                    realTimeQuotes.value = it
                }
        }
    }

    fun destroy() {
        priceManager.popRequest(code)
        orderManager.cancelRequests()
    }

    fun stockInfo(): StockInfo? {
        return stockPool.get(code)
    }

    private fun realTimeTrade(): RealTimeTrade? {
        val stockInfo = stockInfo() ?: return null
        return priceManager.dataMap[stockInfo.code]
    }

    fun price(): Double {
        return realTimeTrade()?.price
            ?: basePrice.value?.output?.price?.toDouble()
            ?: tradeBase.value?.output2?.price?.toDouble()
            ?: 0.0
    }

    // 기준가 (전일 종가)
    fun previousClose(): Double {
        return realTimeTrade()?.previousClose
            ?: basePrice.value?.output?.previousClosePrice?.toDouble()
            ?: 0.0
    }

    fun priceChange(): Double {
        return realTimeTrade()?.delta
            ?: basePrice.value?.output?.priceChange?.toDouble()
            ?: tradeBase.value?.output2?.anticipatedPriceChange?.toDouble()
            ?: 0.0
    }

    fun priceChangeRate(): Double {
        return realTimeTrade()?.rate
            ?: basePrice.value?.output?.priceChangeRate?.toDouble()
            ?: tradeBase.value?.output2?.anticipatedPriceChangeRate?.toDouble()
            ?: 0.0
    }

    fun volume(): Double {
        return realTimeTrade()?.volume
            ?: basePrice.value?.output?.volume?.toDouble()
            ?: tradeBase.value?.output2?.anticipatedVolume?.toDouble()
            ?: 0.0
    }

    fun openPrice(): Double {
        return realTimeTrade()?.open
            ?: basePrice.value?.output?.open?.toDouble()
            ?: tradeBase.value?.output2?.open?.toDouble()
            ?: 0.0
    }

    fun highPrice(): Double {
        return realTimeTrade()?.high
            ?: basePrice.value?.output?.high?.toDouble()
            ?: tradeBase.value?.output2?.high?.toDouble()
            ?: 0.0
    }

    fun lowPrice(): Double {
        return realTimeTrade()?.low
            ?: basePrice.value?.output?.low?.toDouble()
            ?: tradeBase.value?.output2?.low?.toDouble()
            ?: 0.0
    }

    /**
     * 매도 호가와 잔량 10 개
     */
    fun sells(): List<Pair<Double, Double>> {
        return realTimeQuotes.value?.sells()
            ?: tradeBase.value?.output1?.sells()
            ?: empty
    }

    /**
     * 매수 호가와 잔량 10 개
     */
    fun buys(): List<Pair<Double, Double>> {
        return realTimeQuotes.value?.buys()
            ?: tradeBase.value?.output1?.buys()
            ?: empty
    }
}
