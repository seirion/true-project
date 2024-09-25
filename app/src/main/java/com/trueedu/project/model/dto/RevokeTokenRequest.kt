package com.trueedu.project.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RevokeTokenRequest(
    @SerialName(value = "appkey")
    val appKey: String,
    @SerialName(value = "appsecret")
    val appSecret: String,
    val token: String,
)
