package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionId(val value: String) {
    @SerialName("PINGPONG")
    PingPong("PINGPONG"),
    @SerialName("H0STASP0")
    RealTimeQuotes("H0STASP0"), // 실시간 호가 (KRX)
    @SerialName("H0STCNT0")
    RealTimeTrade("H0STCNT0"), // 실시간 체결 (KRX)
    @SerialName("H0NXCNT0")
    RealTimeTradeNxt("H0NXCNT0"), // 실시간 체결 (NXT)
    @SerialName("H0STCNI0")
    TradeNotification("H0STCNI0"), // 체결 통보
}
