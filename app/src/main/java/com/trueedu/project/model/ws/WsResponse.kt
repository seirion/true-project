package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class WsResponse(
    val header: WsResponseHeader,
    val body: WsResponseBody? = null,
) {
    companion object {
        fun from(jsonString: String): WsResponse {
            val json = Json { ignoreUnknownKeys = true }
            return json.decodeFromString<WsResponse>(jsonString)
        }
    }
}

@Serializable
data class WsResponseHeader(
    @SerialName("tr_id")
    val transactionId: TransactionId,
    @SerialName("tr_key")
    val transactionKey: String? = null,
    val datetime: String? = null,
    val encrypt: String? = null,
)

@Serializable
data class WsResponseBody(
    @SerialName("rt_cd")
    val returnCode: String,
    @SerialName("tr_id")
    val transactionId: TransactionId? = null,
    @SerialName("tr_key")
    val transactionKey: String? = null,
    val encrypt: String? = null,
    @SerialName("msg_cd")
    val msgCode: String? = null,
    val msg1: String? = null,
    val output: WsResponseOutput? = null,
)

@Serializable
data class WsResponseOutput(
    val iv: String,
    val key: String,
)
