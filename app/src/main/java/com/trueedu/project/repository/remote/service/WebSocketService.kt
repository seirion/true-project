package com.trueedu.project.repository.remote.service

import okhttp3.WebSocketListener

interface WebSocketService {
    fun connect(listener: WebSocketListener)
    fun sendMessage(message: String)
    fun disconnect()
}
