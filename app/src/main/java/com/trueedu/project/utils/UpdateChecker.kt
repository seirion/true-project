package com.trueedu.project.utils

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 마스터 파일이 업로드 되는 시각 (HHmm)
 */
private val uploadTime = listOf(
    600,
    655,
    735,
    755,
    845,
    946,
    1055,
    1710,
    1730,
    1755,
    1810,
    1830,
    1855,
)

/**
 * 리모트에 더 최신 종목 정보가 있는 지 여부
 *
 */
fun needUpdateRemoteData(localTimestamp: Long, remoteTimestamp: Long): Boolean {

    val localDate = localTimestamp / 10000L
    val remoteDate = remoteTimestamp / 10000L

    // 로컬 데이터가 더 이전 날짜 것이면
    if (localDate < remoteDate) {
        // 주말이면 데이터 업데이트 불필요
        return if (localDate + 1 == remoteDate &&
            (dayOfWeek(localDate) == DayOfWeek.SATURDAY && dayOfWeek(remoteDate) == DayOfWeek.SUNDAY)
        ) {
            false
        } else {
            true
        }
    } else if (localDate == remoteDate) { // 날짜가 같을 때

        val localHHmm = localTimestamp % 10000L
        val remoteHHmm = remoteTimestamp % 10000L

        // Check if remote time falls within the update intervals
        return uploadTime.any { it in (localHHmm + 1)..remoteHHmm }
    } else {
        return false
    }
}

private fun dayOfWeek(yyyyMMdd: Long): DayOfWeek {
    val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    val localDate = LocalDate.parse(yyyyMMdd.toString(), formatter)
    return localDate.dayOfWeek
}
