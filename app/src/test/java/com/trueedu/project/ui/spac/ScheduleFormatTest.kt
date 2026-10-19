package com.trueedu.project.ui.spac

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScheduleFormatTest {
    @Test
    fun `기간은 시작과 종료를 점 표기로 이어서 보여주고 둘 다 없으면 대시`() {
        assertEquals("2026.11.01 ~ 2026.11.15", formatDateRangeOrDash("20261101", "20261115"))
        assertEquals("2026.11.01 ~ -", formatDateRangeOrDash("20261101", ""))
        assertEquals("-", formatDateRangeOrDash("", ""))
    }

    @Test
    fun `DART 검색 URL 은 회사명을 URL 인코딩한다`() {
        val url = dartCompanySearchUrl("예시 컴퍼니")
        assertTrue(url.startsWith("https://dart.fss.or.kr/dsab001/main.do?autoSearch=true&textCrpNM="))
        assertTrue(url.endsWith("%EC%98%88%EC%8B%9C+%EC%BB%B4%ED%8D%BC%EB%8B%88"))
    }

    @Test
    fun `분배금은 필요한 소수 자리만 표시한다`() {
        assertEquals("2,172.4", refundAmountText(2172.4))
        assertEquals("2,150", refundAmountText(2150.0))
        assertEquals("2,150.55", refundAmountText(2150.55))
    }
}
