package com.trueedu.project.data

import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.model.event.WebSocketKeyIssued
import com.trueedu.project.model.ws.RealTimeOrder
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsResponse
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
    companion object {
        private val TAG = WsMessageHandler::class.java.simpleName
    }

    private val event = MutableSharedFlow<WsResponse>()
    fun observeEvent() = event.asSharedFlow()

    // 디버깅 용
    val on = mutableStateOf(false)
    private var foreground = false
    private var stopAt = 0L // background 로 진입한 시각

    // 거래 데이터
    val tradeSignal = MutableSharedFlow<RealTimeTrade>()
    // 호가 데이터
    val quotesSignal = MutableSharedFlow<RealTimeOrder>()

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
        Log.d(TAG, "start")
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
        Log.d(TAG, "stop")
        foreground = false
        webSocketService.disconnect()
        stopAt = SystemClock.elapsedRealtime()
    }

    fun send(jsonString: String) {
        webSocketService.sendMessage(jsonString)
    }

    private fun startWebSocket() {
        Log.d(TAG, "startWebSocket()")

        if (local.webSocketKey.isEmpty()) {
            Log.d(TAG, "websocket key is empty")
            return
        }

        if (!foreground) return

        webSocketService.disconnect()
        webSocketService.connect(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "onOpen()")
                on.value = true
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                Log.d(TAG, "onMessage: $text")
                if (text[0] == '0' || text[0] == '1') { // 실시간체결 or 실시간호가
                    handleRealTimeResponse(text)
                } else { // system message or PINGPONG
                    val res = WsResponse.from(text)
                    Log.d(TAG, "transactionId ${res.header.transactionId}")

                    when (res.header.transactionId) {
                        TransactionId.PingPong -> webSocketService.sendMessage(text)
                        TransactionId.RealTimeQuotes,
                        TransactionId.RealTimeTrade -> {
                            CoroutineScope(Dispatchers.IO).launch {
                                event.emit(res)
                            }
                        }
                        TransactionId.TradeNotification -> {
                            // TODO
                        }
                    }
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d(TAG, "onFailure: ${t.message}")
                on.value = false

                // 실패하였으면 다시 연결 시도 해 본다
                if (foreground) {
                    Log.d(TAG, "retry in 2000ms")
                    MainScope().launch {
                        delay(2000)
                        startWebSocket()
                    }
                }
            }
        })
    }

    private fun handleRealTimeResponse(text: String) {
        val org = text.split("|")
        val transactionId = TransactionId.entries.firstOrNull { it.value == org[1] }
        val data = org[3]
        when (transactionId) {
            TransactionId.RealTimeQuotes -> {
                val dto = RealTimeOrder.from(data)
                MainScope().launch {
                    quotesSignal.emit(dto)
                }
            }
            TransactionId.RealTimeTrade -> {
                val dto = RealTimeTrade.from(data)
                MainScope().launch {
                    tradeSignal.emit(dto)
                }
            }
            else -> {
                // nothing
            }
        }
    }
}
