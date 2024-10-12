package com.trueedu.project.model.dto

import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class WsResponseParseTest {
    @Test
    fun `ping pong message parsing`() {
        val json = Json { ignoreUnknownKeys = true }

        val jsonString = "{\"header\":{\"tr_id\":\"PINGPONG\",\"datetime\":\"20241011121818\"}}"
        val wsResponse = json.decodeFromString<WsResponse>(jsonString)
        assertEquals(TransactionId.PingPong, wsResponse.header.transactionId)
    }

    @Test
    fun `실시간 가격 요청 성공에 대한 응답 처리`() {
        val json = Json { ignoreUnknownKeys = true }

        val jsonString = """{"header":{"tr_id":"H0STASP0","tr_key":"066570","encrypt":"N"},"body":{"rt_cd":"0","msg_cd":"OPSP0000","msg1":"SUBSCRIBE SUCCESS","output":{"iv":"ca513e8bf725c575","key":"uvbpznjqabrxhowcftgehxghhhsxtwro"}}}"""
        val wsResponse = json.decodeFromString<WsResponse>(jsonString)
        assertEquals(TransactionId.RealTimeQuotes, wsResponse.header.transactionId)
        assertEquals("0", wsResponse.body?.returnCode)
        assertEquals("OPSP0000", wsResponse.body?.msgCode)
        assertEquals("ca513e8bf725c575", wsResponse.body?.output?.iv)
        assertEquals("uvbpznjqabrxhowcftgehxghhhsxtwro", wsResponse.body?.output?.key)
    }
}
