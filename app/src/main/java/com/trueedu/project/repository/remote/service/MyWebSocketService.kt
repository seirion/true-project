package com.trueedu.project.repository.remote.service

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject

private const val webSocketUrl = "ws://ops.koreainvestment.com:21000"

class MyWebSocketService @Inject constructor(
    private val okHttpClient: OkHttpClient,
) : WebSocketService {

    companion object {
        private val TAG = MyWebSocketService::class.java.simpleName
    }

    private var webSocket: WebSocket? = null

    override fun connect(listener: WebSocketListener) {
        Log.d(TAG, "connect")
        val request = Request.Builder()
            .url(webSocketUrl)
            .build()
        webSocket = okHttpClient.newWebSocket(request, listener)
    }

    override fun sendMessage(message: String) {
        val result = webSocket?.send("")
    }

    override fun disconnect() {
        Log.d(TAG, "disconnect")
        webSocket?.cancel()
        webSocket = null
    }
}
