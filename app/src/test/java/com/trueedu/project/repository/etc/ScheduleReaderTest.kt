package com.trueedu.project.repository.etc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleReaderTest {
    @Test
    fun `refund json 은 입금일 순으로 정렬된다`() {
        val text = """
            {
              "469900": {"code":"469900","nameKr":"하나31호스팩","refundAmount":2150.5,"date":"20270120","fixed":false},
              "465320": {"code":"465320","nameKr":"교보15호스팩","refundAmount":2172.4,"date":"20261221","fixed":true,"extra":1}
            }
        """.trimIndent()

        val list = parseRefundSchedules(text)

        assertEquals(listOf("465320", "469900"), list.map { it.code })
        assertEquals(2172.4, list[0].refundAmount!!, 0.0)
        assertTrue(list[0].fixed)
        assertEquals(false, list[1].fixed)
    }

    @Test
    fun `refund json 의 분배금이 없으면 null`() {
        val list = parseRefundSchedules("""{"1":{"code":"1","nameKr":"a","date":"20261201"}}""")
        assertNull(list.single().refundAmount)
    }

    @Test
    fun `merge txt 는 JSON 배열을 파싱하고 없는 필드는 빈 문자열`() {
        val text = """
            [
              {"nameKr":"스팩A","code":"111111","target":"회사A","newShareListingDate":"20261229",
               "disclosureUrl":"https://dart.fss.or.kr/x"},
              {"nameKr":"스팩B","code":"222222"}
            ]
        """.trimIndent()

        val list = parseMergeSchedules(text)

        assertEquals(2, list.size)
        assertEquals("회사A", list[0].target)
        assertEquals("20261229", list[0].newShareListingDate)
        assertEquals("", list[1].target)
        assertEquals("", list[1].tradingHaltStartDate)
    }

    @Test
    fun `merge txt 가 비어 있으면 빈 목록`() {
        assertTrue(parseMergeSchedules("").isEmpty())
        assertTrue(parseMergeSchedules("   \n").isEmpty())
    }
}
