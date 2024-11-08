package com.trueedu.project.utils

import org.junit.Assert
import org.junit.Test
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.text.toLong

class UpdateCheckerTest {
    @Test
    fun testRemoteDateMoreRecent() {
        val localTimestamp = toYyyyMMddHHmm("2023-03-15T10:00")
        val remoteTimestamp = toYyyyMMddHHmm("2023-03-16T10:00")
        Assert.assertTrue(needUpdateRemoteData(localTimestamp, remoteTimestamp))
    }

    @Test
    fun testOneDayApartWeekend() {
        val localTimestamp = toYyyyMMddHHmm("2023-03-18T10:00")
        val remoteTimestamp = toYyyyMMddHHmm("2023-03-19T10:00")
        Assert.assertFalse(needUpdateRemoteData(localTimestamp, remoteTimestamp))
    }

    @Test
    fun testSameDateWithinUpdateInterval() {
        val localTimestamp = toYyyyMMddHHmm("2023-03-15T06:00")
        val remoteTimestamp = toYyyyMMddHHmm("2023-03-15T06:55")
        Assert.assertTrue(needUpdateRemoteData(localTimestamp, remoteTimestamp))
    }

    @Test
    fun testSameDateOutsideUpdateInterval() {
        val localTimestamp = toYyyyMMddHHmm("2023-03-15T06:00")
        val remoteTimestamp = toYyyyMMddHHmm("2023-03-15T06:30")
        Assert.assertFalse(needUpdateRemoteData(localTimestamp, remoteTimestamp))
    }

    @Test
    fun testRemoteDateOlder() {
        val localTimestamp = toYyyyMMddHHmm("2023-03-16T10:00")
        val remoteTimestamp = toYyyyMMddHHmm("2023-03-15T10:00")
        Assert.assertFalse(needUpdateRemoteData(localTimestamp, remoteTimestamp))
    }

    private fun toYyyyMMddHHmm(s: String): Long {
        val dateTime = LocalDateTime.parse(s)
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm")
        return dateTime.format(formatter).toLong()
    }
}
