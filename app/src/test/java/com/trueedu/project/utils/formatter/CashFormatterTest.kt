package com.trueedu.project.utils.formatter

import org.junit.Assert.assertEquals
import org.junit.Test

class CashFormatterTest {
    private val cashFormatter: MyFormatter = CashFormatter()

    @Test
    fun `0은 그대로 0으로 표시함`() {
        assertEquals("0", cashFormatter.format(0))
        assertEquals("0", cashFormatter.format(0.0))
    }
    @Test
    fun `자연수 부분만 표시함`() {
        assertEquals("123", cashFormatter.format(123))
        assertEquals("123", cashFormatter.format(123.456))
    }
    @Test
    fun `정수만 표시할 때 소수점 첫 자리에서 반올림한다`() {
        assertEquals("124", cashFormatter.format(123.678))
        assertEquals("124", cashFormatter.format(123.999))
    }
    @Test
    fun `정수 부분 세 자리마다 콤마로 구분이 된다`() {
        assertEquals("123", cashFormatter.format(123))
        assertEquals("1,234", cashFormatter.format(1234))
        assertEquals("12,345", cashFormatter.format(12345))
        assertEquals("123,456", cashFormatter.format(123456))

        assertEquals("1,234,567", cashFormatter.format(1234567))
        assertEquals("1,234,567", cashFormatter.format(1234567.1234))
        assertEquals("1,234,568", cashFormatter.format(1234567.89))
        assertEquals("123,456,789", cashFormatter.format(123456789))
    }
    @Test
    fun `부호를 포함하게 되면 필요한 경우 부호 표시가 앞에 붙는다`() {
        assertEquals("0", cashFormatter.format(0, true))
        assertEquals("0", cashFormatter.format(0.0, true))
        assertEquals("+1,234,567", cashFormatter.format(1234567, true))
        assertEquals("-1,234,567", cashFormatter.format(-1234567, true))
        assertEquals("-1,234,567", cashFormatter.format(-1234567.1234, true))
        assertEquals("+1,234,568", cashFormatter.format(1234567.89, true))
        assertEquals("-1,234,568", cashFormatter.format(-1234567.89, true))
    }
}
