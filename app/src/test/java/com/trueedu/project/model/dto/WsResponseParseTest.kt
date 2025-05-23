package com.trueedu.project.model.dto

import com.trueedu.project.model.ws.RealTimeTrade
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

    @Test
    fun `실시간 가격 요청 성공에 대한 응답 처리`() {
        val jsonString = """
            {
               "header":{
                  "tr_id":"H0STCNT0",
                  "tr_key":"005930",
                  "encrypt":"N"
               },
               "body":{
                  "rt_cd":"0",
                  "msg_cd":"OPSP0000",
                  "msg1":"SUBSCRIBE SUCCESS",
                  "output":{
                     "iv":"4509de06db7218b5",
                     "key":"aodbeofjbfqbecrhgknoobaflnzgdabd"
                  }
               }
            }
        """
        val json = Json { ignoreUnknownKeys = true }
        val wsResponse = json.decodeFromString<WsResponse>(jsonString)
        println(wsResponse)
        assertEquals(TransactionId.RealTimeTrade, wsResponse.header.transactionId)
        assertEquals("005930", wsResponse.header.transactionKey)

        assertEquals("0", wsResponse.body?.returnCode)
        assertEquals("OPSP0000", wsResponse.body?.msgCode)
        assertEquals("4509de06db7218b5", wsResponse.body?.output?.iv)
        assertEquals("aodbeofjbfqbecrhgknoobaflnzgdabd", wsResponse.body?.output?.key)
    }

    @Test
    fun `실시간 체결 데이터 처리`() {
        val msg =
            "0|H0STCNT0|001|066570^150550^98900^2^400^0.41^98826.69^99000^99400^98300^99000^98900^45^246334^24344205800^10052^4970^-5082^53.22^159109^84682^5^0.35^49.34^090014^5^-100^093207^5^-500^103516^2^600^20241014^20^N^2558^705^27882^26123^0.15^451481^54.56^0^^99000"
        val parsed = msg.split("|")

        val transactionId = parsed[1]
        assertEquals("H0STCNT0", transactionId)
        val rawData = parsed[3]
        val trade = RealTimeTrade.from(rawData)

        //val dataParsed = data.split("^")
        val filedName = "유가증권단축종목코드|주식체결시간|주식현재가|전일대비부호|전일대비|전일대비율|가중평균주식가격|주식시가|주식최고가|주식최저가|매도호가1|매수호가1|체결거래량|누적거래량|누적거래대금|매도체결건수|매수체결건수|순매수체결건수|체결강도|총매도수량|총매수수량|체결구분|매수비율|전일거래량대비등락율|시가시간|시가대비구분|시가대비|최고가시간|고가대비구분|고가대비|최저가시간|저가대비구분|저가대비|영업일자|신장운영구분코드|거래정지여부|매도호가잔량|매수호가잔량|총매도호가잔량|총매수호가잔량|거래량회전율|전일동시간누적거래량|전일동시간누적거래량비율|시간구분코드|임의종료구분코드|정적VI발동기준가"
            .split('|')

        /*
        println(
            filedName.zip(dataParsed).mapIndexed { index, (field, value) -> "$index. $field     $value" }
                .joinToString("\n")
        )
         */
        val tolerance = 0.0001

        assertEquals(filedName.size, trade.data.size)
        // 단축코드
        assertEquals("066570", trade.code)
        // 주식체결시간
        assertEquals("150550", trade.datetime)
        // 주식현재가
        assertEquals(98900.0, trade.price, tolerance)
        // 전일대비
        assertEquals(400.0, trade.delta, tolerance)
        // 전일대비율(%)
        assertEquals(0.41, trade.rate, tolerance)
    }
}
