package com.trueedu.project.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

class DateTimeUtilsTest {
    /*
    @Test
    fun testValidDateString() {
        val dateString = "2023-12-28 10:30:00"
        val expectedDateTime = ZonedDateTime.of(
            2023, 12, 28, 10, 30, 0, 0,
            ZoneId.of("Asia/Seoul") // KST
        )
        val expectedDate = Date.from(expectedDateTime.toInstant())
        val actualDate = parseDateString(dateString)
        assertEquals(expectedDate, actualDate)
    }
     */

    @Test
    fun testInvalidDateString() {
        val dateString = "invalid_date_string"
        val actualDate = parseDateString(dateString)
        assertNull(actualDate)
    }

    @Test
    fun testNullDateString() {
        val dateString = null
        val actualDate = parseDateString(dateString)
        assertNull(actualDate) // or assertThrows<IllegalArgumentException> { parseDateString(dateString) }
    }
}
