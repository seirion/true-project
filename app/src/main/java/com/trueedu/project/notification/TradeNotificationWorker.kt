package com.trueedu.project.notification

import android.util.Log
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.realtime.WsMessageHandler
import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsRequest
import com.trueedu.project.model.ws.WsRequestBody
import com.trueedu.project.model.ws.WsRequestBodyInput
import com.trueedu.project.model.ws.WsRequestHeader
import com.trueedu.project.repository.local.Local
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * WsMessageHandler의 orderExecutionSignal을 구독하여
 * 내 주문 체결 이벤트(H0STCNI0)를 notification으로 표시하는 컴포넌트
 */
@Singleton
class TradeNotificationWorker @Inject constructor(
    private val wsMessageHandler: WsMessageHandler,
    private val stockPool: StockPool,
    private val tradeNotificationManager: TradeNotificationManager,
    private val tokenKeyManager: TokenKeyManager,
    private val local: Local,
) {
    companion object {
        private val TAG = TradeNotificationWorker::class.java.simpleName
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        Log.d(TAG, "start()")

        subscribeTradeNotification()

        job = scope.launch {
            wsMessageHandler.orderExecutionSignal.collect { notification ->
                Log.d(TAG, "order execution received: ${notification.code} ${notification.execQty}주 @${notification.execPrice}")
                val stockName = stockPool.get(notification.code)?.nameKr
                tradeNotificationManager.showOrderExecutionNotification(notification, stockName)
            }
        }
    }

    fun stop() {
        Log.d(TAG, "stop()")
        unsubscribeTradeNotification()
        job?.cancel()
        job = null
    }

    /** H0STCNI0 체결통보 구독 요청 (tr_key = HTS ID) */
    private fun subscribeTradeNotification() {
        val htsId = tokenKeyManager.userKey.value?.htsId
        if (htsId.isNullOrEmpty()) {
            Log.w(TAG, "htsId is null or empty, skip H0STCNI0 subscription")
            return
        }
        wsMessageHandler.send(makeTradeNotificationRequest(htsId, subscribe = true))
        Log.d(TAG, "H0STCNI0 subscribed for htsId=$htsId")
    }

    /** H0STCNI0 체결통보 구독 해제 */
    private fun unsubscribeTradeNotification() {
        val htsId = tokenKeyManager.userKey.value?.htsId ?: return
        wsMessageHandler.send(makeTradeNotificationRequest(htsId, subscribe = false))
        Log.d(TAG, "H0STCNI0 unsubscribed for htsId=$htsId")
    }

    private fun makeTradeNotificationRequest(htsId: String, subscribe: Boolean): String {
        val header = WsRequestHeader(
            approvalKey = local.webSocketKey,
            customerType = "P",
            transactionType = if (subscribe) "1" else "2",
            contentType = "utf-8",
        )
        val input = WsRequestBodyInput(
            transactionId = TransactionId.TradeNotification,
            transactionKey = htsId,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }
}
