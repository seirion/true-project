package com.trueedu.project.data

import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsRequest
import com.trueedu.project.model.ws.WsRequestBody
import com.trueedu.project.model.ws.WsRequestBodyInput
import com.trueedu.project.model.ws.WsRequestHeader
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.WebSocketService
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
    private val webSocketService: WebSocketService,
) {

    companion object {
        private const val MAX_SIZE = 20 // 최대 20개의 요청 가능
    }

    /**
     * screenName to List<ticker>
     */
    private val requestStack: ArrayDeque<Pair<String, List<String>>> = ArrayDeque()

    private val requests = mutableSetOf<String>() // key: ticker

    fun start() {
        request()
    }

    fun stop() {
        cancel()
    }

    /**
     * 기존에 있떤 요청을 취소하고 새 요청을 추가함
     */
    fun pushRequest(name: String, codes: List<String>) {
        // 기존 처리 중단
        if (requestStack.isNotEmpty()) {
            cancel()
        }

        // 최대 개수까지만
        val codesRequested = codes.take(MAX_SIZE)

        // 데이터 추가
        requests.clear()
        requests.addAll(codesRequested)
        requestStack.add(name to codesRequested)

        request()
    }

    /**
     * 현재 요청을 취소하고 예전 요청을 복구
     */
    fun popRequest() {
        // 현재 요청 취소
        cancel()

        requestStack.removeLast()

        if (requestStack.isNotEmpty()) {
            requests.addAll(requestStack.last().second)
            request()
        }
    }

    private fun request() {
        requests.forEach {
            webSocketService.sendMessage(makeRequest(it, true))
        }
    }

    private fun cancel() {
        requests.forEach {
            webSocketService.sendMessage(makeRequest(it, false))
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
            transactionType = transactionType,
        )
        val input = WsRequestBodyInput(
            transactionId = TransactionId.RealTimeQuotes,
            transactionKey = code,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }

}
