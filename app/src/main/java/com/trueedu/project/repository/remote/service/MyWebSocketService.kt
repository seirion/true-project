package com.trueedu.project.repository.remote.service

import com.trueedu.project.data.log.logD
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject


class MyWebSocketService @Inject constructor(
    private val webSocketUrl: String,
    private val okHttpClient: OkHttpClient,
) : WebSocketService {

    private var webSocket: WebSocket? = null

    override fun connect(listener: WebSocketListener) {
        logD("connect")
        val request = Request.Builder()
            .url(webSocketUrl)
            .build()
        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    override fun sendMessage(message: String) {
        logD("sendMessage: $message")
        val result = webSocket?.send(message)
    }

    override fun disconnect() {
        logD("disconnect")
        webSocket?.cancel()
        webSocket = null
    }
}
