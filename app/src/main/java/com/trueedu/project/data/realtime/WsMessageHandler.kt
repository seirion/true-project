package com.trueedu.project.data.realtime

import android.os.SystemClock
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.log.logD
import com.trueedu.project.data.log.logE
import com.trueedu.project.model.event.WebSocketKeyIssued
import com.trueedu.project.model.ws.RealTimeIndex
import com.trueedu.project.model.ws.RealTimeOrder
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.model.ws.TradeNotification
import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsResponse
import com.trueedu.project.utils.decryptAes
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.WebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WsMessageHandler @Inject constructor(
    private val local: Local,
    private val tokenKeyManager: TokenKeyManager,
    private val webSocketService: WebSocketService,
) {
    private val event = MutableSharedFlow<WsResponse>()
    fun observeEvent() = event.asSharedFlow()

    // 디버깅 용
    val on = mutableStateOf(false)
    private var foreground = false
    private var stopAt = 0L // background 로 진입한 시각

    // 거래 데이터 (시세 체결 - 구독 종목 전체)
    val tradeSignal = MutableSharedFlow<RealTimeTrade>()
    // 호가 데이터
    val quotesSignal = MutableSharedFlow<RealTimeOrder>()
    // 내 주문 체결 통보 (H0STCNI0)
    val orderExecutionSignal = MutableSharedFlow<TradeNotification>()
    // 실시간 업종지수 (H0UPCNT0)
    val indexSignal = MutableSharedFlow<RealTimeIndex>()

    // 체결통보 AES 복호화 키/IV (구독 응답에서 수신)
    private var tradeNotificationKey: String? = null
    private var tradeNotificationIv: String? = null

    init {
        MainScope().launch {
            tokenKeyManager.observeTokenKeyEvent()
                .collectLatest {
                    when (it) {
                        is WebSocketKeyIssued -> {
                            startWebSocket()
                        }
                        else -> {
                            // nothing to do
                        }
                    }
                }
        }
    }

    // 앱이 foreground 상태가 될 때
    fun start() {
        logD("start")
        foreground = true

        val current = SystemClock.elapsedRealtime()
        MainScope().launch {
            if (current < stopAt + 2000) {
                delay(2000)
            }
            startWebSocket()
        }
    }

    // 앱이 background 상태가 될 때
    fun stop() {
        logD("stop")
        foreground = false
        webSocketService.disconnect()
        stopAt = SystemClock.elapsedRealtime()
    }

    fun send(jsonString: String) {
        webSocketService.sendMessage(jsonString)
    }

    private fun startWebSocket() {
        logD("startWebSocket()")

        if (local.webSocketKey.isEmpty()) {
            logD("websocket key is empty")
            return
        }

        if (!foreground) return

        // 이미 연결된 상태면 재연결 불필요
        if (on.value) {
            logD("already connected, skip")
            return
        }

        webSocketService.disconnect()
        webSocketService.connect(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                logD("onOpen()")
                on.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                logD("onMessage: $text")
                if (text[0] == '1') { // 암호화된 실시간 데이터 (체결통보)
                    handleEncryptedRealTimeResponse(text)
                } else if (text[0] == '0') { // 비암호화 실시간 데이터 (시세체결, 호가)
                    handleRealTimeResponse(text)
                } else { // system message or PINGPONG
                    val res = WsResponse.from(text)
                    logD("transactionId ${res.header.transactionId}")

                    // approval key 만료/무효 시 키 재발급 후 재연결
                    if (res.body?.returnCode == "1" && res.body.msgCode == "OPSP0011") {
                        logD("invalid approval key - reissue and reconnect")
                        local.webSocketKey = ""
                        MainScope().launch {
                            tokenKeyManager.reissueWebSocketKey()
                        }
                        return
                    }

                    when (res.header.transactionId) {
                        TransactionId.PingPong -> webSocketService.sendMessage(text)
                        TransactionId.RealTimeQuotes,
                        TransactionId.RealTimeQuotesNxt,
                        TransactionId.RealTimeTrade,
                        TransactionId.RealTimeTradeNxt -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                event.emit(res)
                            }
                        }
                        TransactionId.TradeNotification,
                        TransactionId.TradeNotificationTest -> {
                            // 체결통보 구독 응답: AES key/iv 저장
                            val iv = res.body?.output?.iv
                            val key = res.body?.output?.key
                            if (!iv.isNullOrEmpty() && !key.isNullOrEmpty()) {
                                tradeNotificationIv = iv
                                tradeNotificationKey = key
                                logD("TradeNotification AES key/iv saved")
                            }
                        }
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                logE(t, "onFailure: ${t.message}")
                on.value = false

                // 실패하였으면 다시 연결 시도 해 본다
                if (foreground) {
                    logD("retry in 2000ms")
                    MainScope().launch {
                        delay(2000)
                        startWebSocket()
                    }
                }
            }
        })
    }

    /**
     * 암호화된 실시간 데이터 처리 (첫 번째 문자가 '1')
     * 현재는 체결통보(H0STCNI0)만 암호화됨
     */
    private fun handleEncryptedRealTimeResponse(text: String) {
        val org = text.split("|")
        val transactionId = TransactionId.entries.firstOrNull { it.value == org[1] }
        val encryptedData = org[3]

        when (transactionId) {
            TransactionId.TradeNotification,
            TransactionId.TradeNotificationTest -> {
                val key = tradeNotificationKey
                val iv = tradeNotificationIv
                if (key.isNullOrEmpty() || iv.isNullOrEmpty()) {
                    logE("TradeNotification AES key/iv not ready, skipping")
                    return
                }
                val decrypted = decryptAes(encryptedData, key, iv)
                if (decrypted.isEmpty()) {
                    logE("TradeNotification AES decryption failed")
                    return
                }
                val notification = TradeNotification.from(decrypted)
                // pValue[13] == "2" 인 경우만 실제 체결 통보
                if (TradeNotification.isExecution(notification.data)) {
                    logD("OrderExecution: ${notification.code} ${notification.execQty}주 @${notification.execPrice}")
                    MainScope().launch {
                        orderExecutionSignal.emit(notification)
                    }
                }
            }
            else -> {
                logD("Unknown encrypted transactionId: ${org[1]}")
            }
        }
    }

    private fun handleRealTimeResponse(text: String) {
        val org = text.split("|")
        val transactionId = TransactionId.entries.firstOrNull { it.value == org[1] }
        val data = org[3]
        when (transactionId) {
            TransactionId.RealTimeQuotes,
            TransactionId.RealTimeQuotesNxt -> {
                val dto = RealTimeOrder.from(data)
                MainScope().launch {
                    quotesSignal.emit(dto)
                }
            }
            TransactionId.RealTimeTrade,
            TransactionId.RealTimeTradeNxt -> {
                val dto = RealTimeTrade.from(data)
                MainScope().launch {
                    tradeSignal.emit(dto)
                }
            }
            TransactionId.RealTimeIndex -> {
                val dto = RealTimeIndex.from(data)
                MainScope().launch {
                    indexSignal.emit(dto)
                }
            }
            else -> {
                // nothing
            }
        }
    }
}
