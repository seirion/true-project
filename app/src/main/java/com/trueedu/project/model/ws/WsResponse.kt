package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WsResponse(
    val header: WsResponseHeader,
    val body: WsResponseBody? = null,
)

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
    @SerialName("tr_id")
    val transactionId: String,
    @SerialName("tr_key")
    val transactionKey: String,
    val encrypt: String?,
)
