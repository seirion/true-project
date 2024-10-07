package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WsRequest(
    val header: WsHeader,
    val body: WsBody,
)

@Serializable
data class WsHeader(
    @SerialName("approval_key")
    val approvalKey: String,
    @Suppress("SpellCheckingInspection")
    @SerialName("custtype")
    val consumerType: String,
    @SerialName("tr_type")
    val trType: String,
    @SerialName("content-type")
    val contentType: String
)

@Serializable
data class WsBody(
    val input: WsBodyInput,
)

@Serializable
data class WsBodyInput(
    @SerialName("tr_id")
    val trId: String,
    @SerialName("tr_key")
    val trKey: String
)