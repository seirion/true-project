package com.trueedu.project.model.dto

import com.trueedu.project.model.dto.firebase.StockInfoKospi
import org.junit.Assert.assertEquals
import org.junit.Test

class StockInfoParseTest {

    // 2024.09.07 기준값
    val stockInfoStr =
            "005930   KR7005930003삼성전자                                ST1000000130000YNN5YYY YYNNNNNNN0NNNNNNNY0000647000000100001NNN00NNN000000020Y0900000375660160000000001001975061100000000596978200000000077804668500012       0 NYY00145983900017049900019302116596000008.9520240630003862449511NNN"

    @Test
    fun `삼성전자 데이터 파싱 테스트`() {
        val stockInfo = StockInfoKospi.from(stockInfoStr)

        assertEquals("005930", stockInfo.code)
        assertEquals("삼성전자", stockInfo.nameKr)
        assertEquals("3862449", stockInfo.marketCap())
        assertEquals("19750611", stockInfo.listingDate())
        assertEquals("000064700", stockInfo.prevPrice())
        assertEquals("000037566016", stockInfo.prevVolume())
    }
}

