package com.trueedu.project.repository.etc

import com.trueedu.project.data.log.logE
import com.trueedu.project.model.dto.spac.RefundSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private val json = Json { ignoreUnknownKeys = true }

private const val REFUND_SCHEDULE_URL =
    "https://raw.githubusercontent.com/true-education/true-education.github.io/refs/heads/main/data/refund.json"

/**
 * refund.json 을 읽어서 입금일 순의 청산 일정 목록으로 변환한다. 실패하면 빈 목록.
 */
suspend fun readRefundSchedules(): List<RefundSchedule> = withContext(Dispatchers.IO) {
    return@withContext try {
        val connection = URL(REFUND_SCHEDULE_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val text = connection.inputStream.bufferedReader().use { it.readText() }
        parseRefundSchedules(text)
    } catch (e: Exception) {
        logE(e, "Failed to read refund schedules")
        emptyList()
    }
}

/** refund.json 본문을 입금일 순으로 정렬된 청산 일정으로 변환 */
internal fun parseRefundSchedules(text: String): List<RefundSchedule> =
    json.decodeFromString(
        MapSerializer(String.serializer(), RefundSchedule.serializer()),
        text
    ).values.sortedBy { it.date }
