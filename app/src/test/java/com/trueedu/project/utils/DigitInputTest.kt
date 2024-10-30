package com.trueedu.project.utils

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * 호가 단위 테스트
 */
class DigitInputTest {
    @Test
    fun `호가 증가 테스트`() {
        assertEquals("1", increasePrice("0"))
        assertEquals("2000", increasePrice("1999"))
        assertEquals("2005", increasePrice("2000"))
        assertEquals("5000", increasePrice("4995"))
        assertEquals("5010", increasePrice("5000"))
        assertEquals("20000", increasePrice("19990"))
        assertEquals("20050", increasePrice("20000"))
        assertEquals("50000", increasePrice("49950"))
        assertEquals("50100", increasePrice("50000"))
        assertEquals("200000", increasePrice("199900"))
        assertEquals("200500", increasePrice("200000"))
        assertEquals("500000", increasePrice("499500"))
        assertEquals("501000", increasePrice("500000"))
    }

    @Test
    fun `호가 감소 테스트`() {
        assertEquals("0", decreasePrice("0"))
        assertEquals("0", decreasePrice("1"))
        assertEquals("1999", decreasePrice("2000"))
        assertEquals("2000", decreasePrice("2005"))
        assertEquals("4995", decreasePrice("5000"))
        assertEquals("5000", decreasePrice("5010"))
        assertEquals("19990", decreasePrice("20000"))
        assertEquals("20000", decreasePrice("20050"))
        assertEquals("49950", decreasePrice("50000"))
        assertEquals("50000", decreasePrice("50100"))
        assertEquals("199900", decreasePrice("200000"))
        assertEquals("200000", decreasePrice("200500"))
        assertEquals("499500", decreasePrice("500000"))
        assertEquals("500000", decreasePrice("501000"))
    }
}
