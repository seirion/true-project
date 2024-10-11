package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionId {
    @SerialName("PINGPONG")
    PingPong,
    @SerialName("H0STASP0")
    RealTimeQuotes, // 실시간 호가
}
