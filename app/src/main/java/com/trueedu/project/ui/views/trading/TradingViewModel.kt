package com.trueedu.project.ui.views.trading

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealOrderManager
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.ws.RealTimeTrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TradingViewModel @Inject constructor(
    val stockPool: StockPool,
    val priceManager: RealPriceManager,
    val orderManager: RealOrderManager,
): ViewModel() {

    companion object {
        private val TAG = TradingViewModel::class.java.simpleName
    }

    private var code: String = ""

    init {
        viewModelScope.launch {
        }
    }

    fun init(code: String) {
        this.code = code
        priceManager.pushRequest(
            code,
            listOf(code)
        )
        orderManager.beginRequests(code)
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
