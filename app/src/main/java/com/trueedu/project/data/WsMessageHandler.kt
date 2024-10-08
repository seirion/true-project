package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.model.event.WebSocketKeyIssued
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.WebSocketService
import kotlinx.coroutines.MainScope
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
    private val tokenControl: TokenControl,
    private val webSocketService: WebSocketService,
) {
    companion object {
        private val TAG = WsMessageHandler::class.java.simpleName
    }

    init {
        MainScope().launch {
            tokenControl.observeAuthEvent()
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
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.d(TAG, "onFailure: ${t.message}")
            }
        })
    }
}