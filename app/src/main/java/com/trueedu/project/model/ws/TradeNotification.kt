package com.trueedu.project.model.ws

/**
 * 국내주식 실시간 체결통보 (H0STCNI0)
 *
 * 데이터는 AES256 암호화되어 수신되며, 복호화 후 '^'로 파싱한다.
 *
 * 필드 순서 (체결통보, pValue[13] == '2' 인 경우):
 * [0]  고객ID
 * [1]  계좌번호
 * [2]  주문번호
 * [3]  원주문번호
 * [4]  매도매수구분 (01: 매도, 02: 매수)
 * [5]  정정구분
 * [6]  주문종류
 * [7]  주문조건
 * [8]  주식단축종목코드
 * [9]  체결수량
 * [10] 체결단가
 * [11] 주식체결시간 (HHmmss)
 * [12] 거부여부 (0: 정상, 1: 거부)
 * [13] 체결여부 (1: 주문·정정·취소·거부, 2: 체결)
 * [14] 접수여부
 * [25] 주문가격
 */
class TradeNotification(
    val data: List<String>
) {
    companion object {
        fun from(rawData: String): TradeNotification {
            return TradeNotification(data = rawData.split("^"))
        }

        /** pValue[13] == "2" 이면 체결, 그 외는 주문/정정/취소/거부 접수 통보 */
        fun isExecution(data: List<String>): Boolean {
            return data.getOrNull(13) == "2"
        }
    }

    // 고객 ID (HTS ID)
    val customerId = data.getOrNull(0).orEmpty()
    // 계좌번호
    val accountNum = data.getOrNull(1).orEmpty()
    // 주문번호
    val orderNum = data.getOrNull(2).orEmpty()
    // 매도매수구분: 01-매도, 02-매수
    val buySellType = data.getOrNull(4).orEmpty()
    // 주식단축종목코드
    val code = data.getOrNull(8).orEmpty()
    // 체결수량
    val execQty = data.getOrNull(9)?.toDoubleOrNull() ?: 0.0
    // 체결단가
    val execPrice = data.getOrNull(10)?.toDoubleOrNull() ?: 0.0
    // 주식체결시간 (HHmmss)
    val execTime = data.getOrNull(11).orEmpty()
    // 체결여부: 2 = 체결
    val execYn = data.getOrNull(13).orEmpty()

    val isBuy: Boolean get() = buySellType == "02"
}
