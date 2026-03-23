package com.trueedu.project.notification

import android.util.Log
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.realtime.WsMessageHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WsMessageHandler의 tradeSignal을 구독하여
 * 실시간 체결 이벤트를 notification으로 표시하는 컴포넌트
 */
@Singleton
class TradeNotificationWorker @Inject constructor(
    private val wsMessageHandler: WsMessageHandler,
    private val stockPool: StockPool,
    private val tradeNotificationManager: TradeNotificationManager,
) {
    companion object {
        private val TAG = TradeNotificationWorker::class.java.simpleName
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        Log.d(TAG, "start()")

        job = scope.launch {
            wsMessageHandler.tradeSignal.collect { trade ->
                Log.d(TAG, "trade received: ${trade.code} ${trade.price}")
                val stockName = stockPool.get(trade.code)?.nameKr
                tradeNotificationManager.showTradeNotification(trade, stockName)
            }
        }
    }

    fun stop() {
        Log.d(TAG, "stop()")
        job?.cancel()
        job = null
    }
}
