package com.trueedu.project.data.realtime

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import com.trueedu.project.data.log.logD
import com.trueedu.project.model.ws.RealTimeIndex
import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsRequest
import com.trueedu.project.model.ws.WsRequestBody
import com.trueedu.project.model.ws.WsRequestBodyInput
import com.trueedu.project.model.ws.WsRequestHeader
import com.trueedu.project.repository.local.Local
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 실시간 업종지수(코스피/코스닥) 웹소켓 구독 관리
 */
@Singleton
class MarketIndexManager @Inject constructor(
    private val local: Local,
    private val wsMessageHandler: WsMessageHandler,
) {
    companion object {
        private val INDEX_CODES = listOf("0001", "1001") // 코스피, 코스닥
    }

    private var job: Job? = null

    // key: "0001"(코스피) / "1001"(코스닥)
    val dataMap = mutableStateMapOf<String, RealTimeIndex>()

    fun start() {
        job = MainScope().launch(Dispatchers.IO) {
            launch {
                snapshotFlow { wsMessageHandler.on.value }
                    .collect {
                        if (it) {
                            subscribe()
                        }
                    }
            }
            launch {
                wsMessageHandler.indexSignal
                    .collect {
                        dataMap[it.code] = it
                    }
            }
        }

        MainScope().launch(Dispatchers.IO) {
            subscribe()
        }
    }

    fun stop() {
        MainScope().launch(Dispatchers.IO) {
            unsubscribe()

            job?.cancel()
            job = null
        }
    }

    private suspend fun subscribe() {
        logD("MarketIndexManager subscribe")
        INDEX_CODES.forEach {
            wsMessageHandler.send(makeRequest(it, true))
            delay(10)
        }
    }

    private suspend fun unsubscribe() {
        logD("MarketIndexManager unsubscribe")
        INDEX_CODES.forEach {
            wsMessageHandler.send(makeRequest(it, false))
            delay(10)
        }
    }

    private fun makeRequest(code: String, subscribe: Boolean): String {
        val transactionType = if (subscribe) "1" else "2"

        val header = WsRequestHeader(
            approvalKey = local.webSocketKey,
            customerType = "P",
            transactionType = transactionType,
            contentType = "utf-8",
        )
        val input = WsRequestBodyInput(
            transactionId = TransactionId.RealTimeIndex,
            transactionKey = code,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }
}
