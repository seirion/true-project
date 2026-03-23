package com.trueedu.project.data.realtime

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshotFlow
import com.trueedu.project.data.log.logD
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
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
        private const val MAX_SIZE = 20 // 최대 20개의 요청 가능

        /**
         * 현재 시각이 NXT 거래 시간대인지 확인
         *
         * NXT 운영 시간:
         *   - 오전 8:00 ~ 9:00 (KRX 장 시작 전)
         *   - 오후 3:30 ~ 8:00 (KRX 장 마감 후)
         */
        fun isNxtTradingHour(): Boolean {
            val cal = Calendar.getInstance()
            val hour = cal.get(Calendar.HOUR_OF_DAY)
            val minute = cal.get(Calendar.MINUTE)
            val totalMinutes = hour * 60 + minute

            val morningStart = 8 * 60       // 08:00
            val morningEnd = 9 * 60         // 09:00
            val afternoonStart = 15 * 60 + 30  // 15:30
            val afternoonEnd = 20 * 60      // 20:00

            return totalMinutes in morningStart until morningEnd ||
                    totalMinutes in afternoonStart until afternoonEnd
        }

        fun currentTradeTransactionId(): TransactionId {
            return if (isNxtTradingHour()) {
                TransactionId.RealTimeTradeNxt
            } else {
                TransactionId.RealTimeTrade
            }
        }
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
                snapshotFlow { wsMessageHandler.on.value }
                    .collect {
                        if (it) {
                            resumeRequests()
                        }
                    }
            }
            launch {
                wsMessageHandler.observeEvent()
                    .filter {
                        it.header.transactionId == TransactionId.RealTimeTrade ||
                        it.header.transactionId == TransactionId.RealTimeTradeNxt
                    }
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
        logD("websocket connection recovered")
        MainScope().launch(Dispatchers.IO) {
            beginRequests()
        }
    }

    /**
     * 기존에 있떤 요청을 취소하고 새 요청을 추가함
     */
    fun pushRequest(name: String, codes: List<String>) {
        MainScope().launch(Dispatchers.IO) {
            logD("pushRequest: $name ${codes.size}")
            // 기존 처리 중단
            if (requestStack.isNotEmpty()) {
                cancelRequests()
            }

            // 최대 개수까지만
            val codesRequested = codes.take(MAX_SIZE)

            // 데이터 추가
            requests.clear()
            requests.addAll(codesRequested)

            val topName = requestStack.lastOrNull()?.first
            if (topName == name) {
                // 이미 존재하는 name 이면 replace
                requestStack.removeLast()
            }
            requestStack.add(name to codesRequested)

            beginRequests()
        }
    }

    /**
     * 현재 요청을 취소하고 예전 요청을 복구
     */
    fun popRequest(name: String) {
        logD("popRequest: $name ")
        if (requestStack.isEmpty()) return
        if (requestStack.last().first != name) return

        // 현재 요청 취소
        MainScope().launch(Dispatchers.IO) {
            cancelRequests()

            requestStack.removeLast()

            if (requestStack.isNotEmpty()) {
                requests.addAll(requestStack.last().second)
                beginRequests()
            }
        }
    }

    private suspend fun beginRequests() {
        val requests = requests.toList()
        requests.forEach {
            wsMessageHandler.send(makeRequest(it, true))
            delay(10)
        }
    }

    private suspend fun cancelRequests() {
        val requests = requests.toList()
        requests.forEach {
            wsMessageHandler.send(makeRequest(it, false))
            delay(10)
        }
    }

    /**
     * 요청을 위한 json 데이터 만들기
     * @param code: 종목 코드
     * @param subscribe: true - 구독, false - 해지
     *
     * NXT 운영 시간(08:00~09:00, 15:30~20:00)에는 H0NXCNT0 를 사용하고,
     * 그 외 KRX 정규장 시간에는 H0STCNT0 를 사용한다.
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
            transactionId = currentTradeTransactionId(),
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
