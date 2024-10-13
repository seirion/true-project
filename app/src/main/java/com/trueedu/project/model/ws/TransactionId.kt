package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionId {
    @SerialName("PINGPONG")
    PingPong,
    @SerialName("H0STASP0")
    RealTimeQuotes, // 실시간 호가
    @SerialName("H0STCNT0")
    RealTimeTrade, // 실시간 체결
    @SerialName("H0STCNI0")
    TradeNotification, // 체결 통보
}
