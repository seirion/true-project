package com.trueedu.project.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

fun parseDateString(dateString: String?): Date? {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return try {
        val localDateTime = LocalDateTime.parse(dateString, formatter)
        Date.from(localDateTime.atZone(TimeZone.getDefault().toZoneId()).toInstant())
    } catch (e: Exception) {
        null // 파싱 오류 시 null 반환
    }
}

fun currentTimeToHHmmss(): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("HHmmss")
    return now.format(formatter)
}

private val yyyyMMddFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
fun LocalDate.yyyyMMdd(): String {
    return this.format(yyyyMMddFormatter)
}
