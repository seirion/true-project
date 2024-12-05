package com.trueedu.project.data.realtime

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.model.ws.RealTimeOrder
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

@Singleton
class RealOrderManager @Inject constructor(
    private val local: Local,
    private val wsMessageHandler: WsMessageHandler
) {
    companion object {
        private val TAG = RealOrderManager::class.java.simpleName
    }

    private var job: Job? = null

    // 현재 처리중인 종목 코드
    private var code: String? = null

    val data = mutableStateOf<RealTimeOrder?>(null)

    fun start() {
        job = MainScope().launch(Dispatchers.IO) {
            launch {
                wsMessageHandler.observeEvent()
                    .filter { it.header.transactionId == TransactionId.RealTimeQuotes }
                    .collect {
                        it.body
                        Log.d(TAG, it.toString())
                    }
            }
            launch {
                // 실시간 호가 처리
                wsMessageHandler.quotesSignal
                    .collect {
                        data.value = it
                    }
            }
        }
        if (code != null) {
            beginRequests(code!!)
        }
    }

    fun stop() {
        cancelRequests()
        job?.cancel()
        job = null
    }

    fun beginRequests(code: String) {
        this.code = code
        wsMessageHandler.send(makeRequest(code, true))
    }

    fun cancelRequests() {
        if (code != null) {
            wsMessageHandler.send(makeRequest(code!!, false))
            code = null
        }
    }

    /**
     * 요청을 위한 json 데이터 만들기
     * @param code: 종목 코드
     * @param subscribe: true - 구독, false - 해지
     */
    private fun makeRequest(code: String, subscribe: Boolean): String {
        val transactionType = if (subscribe) "1" else "2"

        val header = WsRequestHeader(
            approvalKey = local.webSocketKey,
            customerType = "P",
            transactionType = transactionType,
            contentType = "utf-8",
        )
        val input = WsRequestBodyInput(
            transactionId = TransactionId.RealTimeQuotes,
            transactionKey = code,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }
}
