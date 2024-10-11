package com.trueedu.project.model.dto

import com.trueedu.project.model.ws.TransactionId
import com.trueedu.project.model.ws.WsResponse
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class WsResponseParseTest {
    @Test
    fun `ping pong message parsing`() {
        val json = Json { ignoreUnknownKeys = true }

        val jsonString = "{\"header\":{\"tr_id\":\"PINGPONG\",\"datetime\":\"20241011121818\"}}"
        val wsResponse = json.decodeFromString<WsResponse>(jsonString)
        assertEquals(TransactionId.PingPong, wsResponse.header.transactionId)
    }
}
