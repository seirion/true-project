package com.trueedu.project.data

import androidx.compose.runtime.mutableStateMapOf
import com.trueedu.project.model.ws.RealTimeTrade
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
 * 실시간 시세 처리를 위한 데이터 관리
 */
@Singleton
class RealPriceManager @Inject constructor(
    private val local: Local,
    private val wsMessageHandler: WsMessageHandler
) {

    companion object {
        private val TAG = RealPriceManager::class.java.simpleName
        private const val MAX_SIZE = 20 // 최대 20개의 요청 가능
    }

    private var job: Job? = null

    /**
     * screenName to List<ticker>
     */
    private val requestStack: ArrayDeque<Pair<String, List<String>>> = ArrayDeque()

    private val requests = mutableSetOf<String>() // key: ticker

    private val decodeTool = mutableMapOf<String, DecodeTool>()

    // key: code
    val dataMap = mutableStateMapOf<String, RealTimeTrade>()

    fun start() {
        job = MainScope().launch(Dispatchers.IO) {
            launch {
                wsMessageHandler.observeEvent()
                    .filter { it.header.transactionId == TransactionId.RealTimeTrade }
                    .collect {
                        if (it.body?.returnCode != "0") return@collect
                        val code = it.body.transactionKey ?: return@collect
                        val iv = it.body.output?.iv ?: return@collect
                        val key = it.body.output.key ?: return@collect
                        decodeTool[code] = DecodeTool(iv, key)
                    }
            }
            launch {
                wsMessageHandler.tradeSignal
                    .collect {
                        dataMap[it.code] = it
                    }
            }
        }

        beginRequests()
    }

    fun stop() {
        cancelRequests()

        job?.cancel()
        job = null
    }

    /**
     * 기존에 있떤 요청을 취소하고 새 요청을 추가함
     */
    fun pushRequest(name: String, codes: List<String>) {
        // 기존 처리 중단
        if (requestStack.isNotEmpty()) {
            cancelRequests()
        }

        // 최대 개수까지만
        val codesRequested = codes.take(MAX_SIZE)

        // 데이터 추가
        requests.clear()
        requests.addAll(codesRequested)
        requestStack.add(name to codesRequested)

        beginRequests()
    }

    /**
     * 현재 요청을 취소하고 예전 요청을 복구
     */
    fun popRequest() {
        // 현재 요청 취소
        cancelRequests()

        if (requestStack.isNotEmpty()) {
            requestStack.removeLast()
            requests.addAll(requestStack.last().second)
            beginRequests()
        }
    }

    private fun beginRequests() {
        requests.forEach {
            wsMessageHandler.send(makeRequest(it, true))
        }
    }

    private fun cancelRequests() {
        requests.forEach {
            wsMessageHandler.send(makeRequest(it, false))
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
            transactionId = TransactionId.RealTimeTrade,
            transactionKey = code,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }

}

data class DecodeTool(
    val iv: String,
    val key: String,
)
