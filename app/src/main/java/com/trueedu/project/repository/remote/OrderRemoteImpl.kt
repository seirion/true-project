package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.OrderService

class OrderRemoteImpl(
    @NormalService
    private val orderService: OrderService
): OrderRemote {

    override fun buy(
        accountNum: String,
        code: String,
        price: Int,
        quantity: Int
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "TTTC0802U", // 매수
            "custtype" to "P",
        )

        /**
         * 00 : 지정가
         * 01 : 시장가
         * 02 : 조건부지정가
         * 03 : 최유리지정가
         * 04 : 최우선지정가
         * 05 : 장전 시간외 (08:20~08:40)
         * 06 : 장후 시간외 (15:30~16:00)
         * 07 : 시간외 단일가(16:00~18:00)
         * 08 : 자기주식
         * 09 : 자기주식S-Option
         * 10 : 자기주식금전신탁
         * 11 : IOC지정가 (즉시체결,잔량취소)
         * 12 : FOK지정가 (즉시체결,전량취소)
         * 13 : IOC시장가 (즉시체결,잔량취소)
         * 14 : FOK시장가 (즉시체결,전량취소)
         * 15 : IOC최유리 (즉시체결,잔량취소)
         * 16 : FOK최유리 (즉시체결,전량취소)
         */
        val queries = mapOf(
            "CANO" to "", // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to "", // 계좌번호 체계(8-2)의 뒤 2자리
            "PDNO" to "", // 종목번호
            "ORD_DVSN" to "00", // 주문 구분 - 일단 지정가로 주문하기
            "ORD_QTY" to "", // 주문 수량
            "ORD_UNPR" to "", // 주문 단가
            "" to "",
        )
        orderService.buy(headers, queries)
    }
}
