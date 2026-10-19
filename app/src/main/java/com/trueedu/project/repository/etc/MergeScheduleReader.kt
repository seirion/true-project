package com.trueedu.project.repository.etc

import com.trueedu.project.data.log.logE
import com.trueedu.project.model.dto.spac.MergeSchedule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

private val json = Json {
    ignoreUnknownKeys = true
    explicitNulls = false
    isLenient = true
}

private const val MERGE_SCHEDULE_URL =
    "https://raw.githubusercontent.com/true-education/true-education.github.io/refs/heads/main/data/merge.txt"

/**
 * merge.txt(JSON 배열 문자열)를 읽어서 합병 일정 목록으로 변환한다. 실패하면 빈 목록.
 */
suspend fun readMergeSchedules(): List<MergeSchedule> = withContext(Dispatchers.IO) {
    return@withContext try {
        val connection = URL(MERGE_SCHEDULE_URL).openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000

        val text = connection.inputStream.bufferedReader().use { it.readText() }
        parseMergeSchedules(text)
    } catch (e: Exception) {
        logE(e, "Failed to read merge schedules")
        emptyList()
    }
}

/** merge.txt 본문을 합병 일정 목록으로 변환. 내용이 비어 있으면 빈 목록 */
internal fun parseMergeSchedules(text: String): List<MergeSchedule> {
    val body = text.trim()
    if (body.isBlank()) return emptyList()
    return json.decodeFromString<List<MergeSchedule>>(body)
}
