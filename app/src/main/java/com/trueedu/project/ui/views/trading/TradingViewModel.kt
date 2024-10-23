package com.trueedu.project.ui.views.trading

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealOrderManager
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.price.TradeResponse
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
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
    val trade = mutableStateOf<TradeResponse?>(null)

    fun init(code: String) {
        this.code = code
        priceManager.pushRequest(
            code,
            listOf(code)
        )
        orderManager.beginRequests(code)

        viewModelScope.launch {
            priceRemote.currentTrade(code)
                .collect {
                    Log.d(TAG, "호가 api: $it")
                    trade.value = it
                }
        }
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
}
