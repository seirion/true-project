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

        val transactionId = parsed[1]
        assertEquals("H0STASP0", transactionId)
        val data = parsed[3]
        val dataParsed = data.split("^")

        // [3..12] 매도 호가, [23..32] 잔량
        // [13..22] 매수 호가, [33..42] 잔량

        // 총매도호가 잔량
        assertEquals("33178", dataParsed[43])
        // 총매도호가 잔량 증감
        assertEquals("0", dataParsed[54])
        // 총매수호가 잔량
        assertEquals("24488", dataParsed[44])
        // 총매수호가 잔량 증감
        assertEquals("3930", dataParsed[55])
        // 시간외 총매도호가 잔량
        assertEquals("0", dataParsed[45])
        // 시간외 총매수호가 증감
        assertEquals("0", dataParsed[46])
        // 시간외 총매도호가 잔량
        assertEquals("0", dataParsed[56])
        // 시간외 총매수호가 증감
        assertEquals("0", dataParsed[57])
        // 예상 체결가
        assertEquals("0", dataParsed[47])
        // 예상 체결량
        assertEquals("0", dataParsed[48])
        // 예상 거래량
        assertEquals("6134", dataParsed[49])
        // 예상체결 대비
        assertEquals("-97200", dataParsed[50])
        // 부호
        assertEquals("5", dataParsed[51])
        // 예상체결 전일대비율
        assertEquals("-100.00", dataParsed[52])
        // 누적거래량
        assertEquals("354177", dataParsed[53])
        // 주식매매 구분코드
        assertEquals("0", dataParsed[58])
    }
}
