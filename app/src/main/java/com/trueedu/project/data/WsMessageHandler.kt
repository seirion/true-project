package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.model.event.WebSocketKeyIssued
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.WebSocketService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
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

    val tradeSignal = MutableSharedFlow<RealTimeTrade>()

    init {
        MainScope().launch {
            tokenKeyManager.observeAuthEvent()
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
        startWebSocket()
    }

    // 앱이 background 상태가 될 때
    fun stop() {
        Log.d(TAG, "stop")
        webSocketService.disconnect()
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

        webSocketService.connect(object: WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "onOpen()")
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
            }
        })
    }

    private fun handleRealTimeResponse(text: String) {
        val org = text.split("|")
        val transactionId = TransactionId.entries.firstOrNull { it.value == org[1] }
        val data = org[3]
        when (transactionId) {
            TransactionId.RealTimeQuotes -> {
                // TODO
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
