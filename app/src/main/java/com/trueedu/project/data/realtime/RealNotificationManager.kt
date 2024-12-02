package com.trueedu.project.data.realtime

import android.util.Log
import androidx.compose.runtime.snapshotFlow
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsRequest
import com.trueedu.project.model.ws.WsRequestBody
import com.trueedu.project.model.ws.WsRequestBodyInput
import com.trueedu.project.model.ws.WsRequestHeader
import com.trueedu.project.repository.local.Local
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 실시간 체결 통보 처리
 */
@Singleton
class RealNotificationManager @Inject constructor(
    private val local: Local,
    private val wsMessageHandler: WsMessageHandler,
    private val tokenKeyManager: TokenKeyManager,
) {

    companion object {
        private val TAG = RealNotificationManager::class.java.simpleName
    }

    private var job: Job? = null

    private val decodeTool: DecodeTool? = null

    fun start() {
        job = MainScope().launch(Dispatchers.IO) {
            launch {
                snapshotFlow { wsMessageHandler.on.value }
                    .collect {
                        if (it) {
                            resumeRequests()
                        }
                    }
            }
            launch {
                wsMessageHandler.observeEvent()
                    .filter { it.header.transactionId == TransactionId.TradeNotification }
                    .collect {
                    }
            }
            launch {
                wsMessageHandler.tradeNotificationSignal
                    .collect {
                    }
            }
        }

        MainScope().launch(Dispatchers.IO) {
            beginRequests()
        }
    }

    fun stop() {
        MainScope().launch(Dispatchers.IO) {
            cancelRequests()

            job?.cancel()
            job = null
        }
    }

    /**
     * 웹소켓이 끊어졌다가 재연결되면 현재의 요청을 다시 시도
     */
    fun resumeRequests() {
        Log.d(TAG, "websocket connection recovered")
        MainScope().launch(Dispatchers.IO) {
            beginRequests()
        }
    }

    /**
     * 기존에 있떤 요청을 취소하고 새 요청을 추가함
     */
    fun pushRequest(name: String, codes: List<String>) {
        MainScope().launch(Dispatchers.IO) {
            Log.d(TAG, "pushRequest: $name ${codes.size}")
            beginRequests()
        }
    }

    private suspend fun beginRequests() {
        val htsId = tokenKeyManager.userKey.value?.htsId ?: return
        wsMessageHandler.send(makeRequest(htsId, true))
    }

    private suspend fun cancelRequests() {
        val htsId = tokenKeyManager.userKey.value?.htsId ?: return
        wsMessageHandler.send(makeRequest(htsId, false))
    }

    /**
     * 요청을 위한 json 데이터 만들기
     * @param subscribe: true - 구독, false - 해지
     */
    private fun makeRequest(htsId: String, subscribe: Boolean): String {

        val transactionType = if (subscribe) "1" else "2"

        val header = WsRequestHeader(
            approvalKey = local.webSocketKey,
            customerType = "P",
            transactionType = transactionType,
            contentType = "utf-8",
        )
        val input = WsRequestBodyInput(
            transactionId = TransactionId.RealTimeTrade,
            transactionKey = htsId,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }
}
