package com.trueedu.project.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HashKeyRequest(
    @SerialName(value = "JsonBody")
    val jsonBody: String,
)
