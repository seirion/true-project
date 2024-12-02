package com.trueedu.project.model.ws

/**
 * 실시간 체결 데이터 (websocket)
 */
class RealTimeNotification(
    val data: List<String>
) {
    companion object {
        fun from(rawData: String): RealTimeNotification {
            return RealTimeNotification(
                data = rawData.split("|")
            )
        }

        // 필드 index 구하기
        private val dataKey = "고객ID|계좌번호|주문번호|원주문번호|매도매수구분|정정구분|주문종류|주문조건|주식단축종목코드|체결수량|체결단가|주식체결시간|거부여부|체결여부|접수여부|지점번호|주문수량|계좌명|체결종목명|신용구분|신용대출일자|체결종목명40|주문가격"
            .split("|")
            .mapIndexed { i, k -> k to i }
            .toMap()
    }

    fun htsId() = getAttributes("고객ID")
    fun accountNum() = getAttributes("계좌번호")
    fun transactionId() = getAttributes("주문번호")
    fun isBuy() = getAttributes("매도매수구분") == "매수"
    fun isSell() = getAttributes("매도매수구분") == "매도"
    val code: String?
        get() = getAttributes("주식단축종목코드")

    fun tradeQuantity() = getAttributes("체결수량")
    fun tradePrice() = getAttributes("체결단가")
    fun tradeTime() = getAttributes("주식체결시간")
    fun orderQuantity() = getAttributes("주문수량")
    fun orderPrice() = getAttributes("주문가격")

    val nameKr: String?
        get() = getAttributes("체결종목명")

    // 데이터 필드 스트링으로 data 받기
    private fun getAttributes(s: String): String? {
        return dataKey[s]?.let { index ->
            data.getOrNull(index)
        }
    }
}
