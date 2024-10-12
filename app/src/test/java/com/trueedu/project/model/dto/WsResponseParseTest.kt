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
    fun `실시간 호가 요청 성공에 대한 응답 처리`() {
        val json = Json { ignoreUnknownKeys = true }

        val jsonString = """{"header":{"tr_id":"H0STASP0","tr_key":"066570","encrypt":"N"},"body":{"rt_cd":"0","msg_cd":"OPSP0000","msg1":"SUBSCRIBE SUCCESS","output":{"iv":"ca513e8bf725c575","key":"uvbpznjqabrxhowcftgehxghhhsxtwro"}}}"""
        val wsResponse = json.decodeFromString<WsResponse>(jsonString)
        assertEquals(TransactionId.RealTimeQuotes, wsResponse.header.transactionId)
        assertEquals("0", wsResponse.body?.returnCode)
        assertEquals("OPSP0000", wsResponse.body?.msgCode)
        assertEquals("ca513e8bf725c575", wsResponse.body?.output?.iv)
        assertEquals("uvbpznjqabrxhowcftgehxghhhsxtwro", wsResponse.body?.output?.key)
    }

    @Test
    fun `실시간 호가 데이터 처리`() {
        val msg = "0|H0STASP0|001|066570^121523^0^99100^99200^99300^99400^99500^99600^99700^99800^99900^100000^98900^98800^98700^98600^98500^98400^98300^98200^98100^98000^1244^1096^775^2090^3584^2831^4081^4526^5637^7314^1108^4129^4670^2050^1665^995^3837^678^1288^4068^33178^24488^0^0^0^0^6134^-97200^5^-100.00^354177^0^3930^0^0^0"
        val parsed = msg.split("|")
        println(parsed)

        val transactionId = parsed[1]
        assertEquals("H0STASP0", transactionId)
        val data = parsed[3]
        val dataParsed = data.split("^")

        // [3..12] 매도 호가, [23..32] 잔량
        // [13..22] 매수 호가, [33..42] 잔량

        println("총매도호가 잔량        " + (dataParsed[43]))
        println("총매도호가 잔량 증감   " + (dataParsed[54]))
        println("총매수호가 잔량        " + (dataParsed[44]))
        println("총매수호가 잔량 증감   " + (dataParsed[55]))
        println("시간외 총매도호가 잔량 " + (dataParsed[45]))
        println("시간외 총매수호가 증감 " + (dataParsed[46]))
        println("시간외 총매도호가 잔량 " + (dataParsed[56]))
        println("시간외 총매수호가 증감 " + (dataParsed[57]))
        println("예상 체결가            " + (dataParsed[47]))
        println("예상 체결량            " + (dataParsed[48]))
        println("예상 거래량            " + (dataParsed[49]))
        println("예상체결 대비          " + (dataParsed[50]))
        println("부호                   " + (dataParsed[51]))
        println("예상체결 전일대비율    " + (dataParsed[52]))
        println("누적거래량             " + (dataParsed[53]))
        println("주식매매 구분코드      " + (dataParsed[58]))
    }
}
