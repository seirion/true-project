package com.trueedu.project.data.realtime

import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.log.logD
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
        /**
         * 현재 시각 기준 호가 TransactionId 반환
         *
         * NXT 운영 시간(08:00~09:00, 15:30~20:00)에는 H0NXASP0 (NXT 호가),
         * 그 외에는 H0STASP0 (KRX 호가) 사용
         */
        fun currentQuotesTransactionId(): TransactionId {
            return if (RealPriceManager.isNxtTradingHour()) {
                TransactionId.RealTimeQuotesNxt
            } else {
                TransactionId.RealTimeQuotes
            }
        }
    }

    private var job: Job? = null

    // 현재 처리중인 종목 코드
    private var code: String? = null

    val data = mutableStateOf<RealTimeOrder?>(null)

    fun start() {
        job = MainScope().launch(Dispatchers.IO) {
            launch {
                wsMessageHandler.observeEvent()
                    .filter {
                        it.header.transactionId == TransactionId.RealTimeQuotes ||
                        it.header.transactionId == TransactionId.RealTimeQuotesNxt
                    }
                    .collect {
                        it.body
                        logD(it.toString())
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
     *
     * NXT 운영 시간(08:00~09:00, 15:30~20:00)에는 H0NXASP0 를 사용하고,
     * 그 외 KRX 정규장 시간에는 H0STASP0 를 사용한다.
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
            transactionId = currentQuotesTransactionId(),
            transactionKey = code,
        )
        val body = WsRequestBody(input)
        return Json.encodeToString(WsRequest(header, body))
    }
}
