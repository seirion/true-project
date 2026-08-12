package com.trueedu.project.model.ws

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionId(val value: String) {
    @SerialName("PINGPONG")
    PingPong("PINGPONG"),
    @SerialName("H0STASP0")
    RealTimeQuotes("H0STASP0"), // 실시간 호가 (KRX)
    @SerialName("H0NXASP0")
    RealTimeQuotesNxt("H0NXASP0"), // 실시간 호가 (NXT)
    @SerialName("H0STCNT0")
    RealTimeTrade("H0STCNT0"), // 실시간 체결 (KRX)
    @SerialName("H0NXCNT0")
    RealTimeTradeNxt("H0NXCNT0"), // 실시간 체결 (NXT)
    @SerialName("H0STCNI0")
    TradeNotification("H0STCNI0"), // 체결 통보 (실전)
    @SerialName("H0STCNI9")
    TradeNotificationTest("H0STCNI9"), // 체결 통보 (모의)
    @SerialName("H0UPCNT0")
    RealTimeIndex("H0UPCNT0"), // 실시간 업종지수
}
