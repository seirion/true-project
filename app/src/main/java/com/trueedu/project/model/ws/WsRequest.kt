package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WsRequest(
    val header: WsRequestHeader,
    val body: WsRequestBody,
)

@Serializable
data class WsRequestHeader(
    @SerialName("approval_key")
    val approvalKey: String,
    @SerialName("custtype")
    val customerType: String = "P", // 고정 - 개인
    @SerialName("tr_type")
    val transactionType: String, // 1-등록, 2-해제
    @SerialName("content-type")
    val contentType: String = "utf-8", // 고정
)

@Serializable
data class WsRequestBody(
    val input: WsRequestBodyInput
)

@Serializable
data class WsRequestBodyInput(
    @SerialName("tr_id")
    val transactionId: TransactionId,
    @SerialName("tr_key")
    val transactionKey: String,
)
