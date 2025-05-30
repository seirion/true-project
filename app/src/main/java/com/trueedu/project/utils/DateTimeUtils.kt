package com.trueedu.project.utils

import java.text.SimpleDateFormat
import java.time.DayOfWeek
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

private val yyyyMMddHHmmFormatter = SimpleDateFormat("yyyyMMddHHmm", Locale.getDefault())
fun Date.yyyyMMddHHmm(): String {
    return yyyyMMddHHmmFormatter.format(this)
}

fun stringToLocalDate(yyyyMMdd: String): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    return LocalDate.parse(yyyyMMdd, formatter)
}

// 오전 9:00 이전까지는 이전 날포함 가장 최근 장이 열리는 날로 결정
fun latestWorkDay(): LocalDate {
    val now = LocalDateTime.now()
    var date = if (now.hour < 9) {
        now.toLocalDate().minusDays(1)
    } else {
        now.toLocalDate()
    }
    while (date.isHoliday()) {
        date = date.minusDays(1)
    }
    return date
}

fun LocalDate.isHoliday(): Boolean {
    return when (this.dayOfWeek) {
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY -> true
        else -> {
            holidays.contains(this)
        }
    }
}

// 몇 개 안 되니까 그냥 하드 코딩
// 주식 장이 열리지 않는 날
private val holidays = setOf(
    LocalDate.of(2025, 5, 5),
    LocalDate.of(2025, 5, 6),
    LocalDate.of(2025, 6, 3),
    LocalDate.of(2025, 6, 6),
    LocalDate.of(2025, 8, 15),
    LocalDate.of(2025, 10, 3),
    LocalDate.of(2025, 10, 6),
    LocalDate.of(2025, 10, 7),
    LocalDate.of(2025, 10, 8),
    LocalDate.of(2025, 10, 9),
    LocalDate.of(2025, 12, 25),
    LocalDate.of(2025, 12, 31),
    LocalDate.of(2026, 1, 1),
    LocalDate.of(2026, 1, 1),
    LocalDate.of(2026, 2, 16),
    LocalDate.of(2026, 2, 17),
    LocalDate.of(2026, 2, 18),
    LocalDate.of(2026, 3, 2),
    LocalDate.of(2026, 5, 5),
    LocalDate.of(2026, 5, 25),
    LocalDate.of(2026, 8, 17),
    LocalDate.of(2026, 9, 24),
    LocalDate.of(2026, 9, 25),
    LocalDate.of(2026, 10, 9),
    LocalDate.of(2026, 12, 25),
    LocalDate.of(2026, 12, 31),
)
