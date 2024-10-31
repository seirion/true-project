package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.OrderService
import kotlinx.coroutines.flow.Flow

class OrderRemoteImpl(
    @NormalService
    private val orderService: OrderService
): OrderRemote {

    override fun buy(
        accountNum: String,
        code: String,
        price: String,
        quantity: String,
    ): Flow<OrderResponse> {
        return buySell(
            isBuy = true,
            accountNum = accountNum,
            code = code,
            price = price,
            quantity = quantity,
        )
    }

    override fun sell(
        accountNum: String,
        code: String,
        price: String,
        quantity: String,
    ): Flow<OrderResponse> {
        return buySell(
            isBuy = false,
            accountNum = accountNum,
            code = code,
            price = price,
            quantity = quantity,
        )
    }

    private fun buySell(
        isBuy: Boolean,
        accountNum: String,
        code: String,
        price: String,
        quantity: String,
    ) = apiCallFlow {

        // 현금 매수, 현금 매도
        val transactionId = if (isBuy) "TTTC0802U" else "TTTC0801U"
        val headers = mapOf(
            "tr_id" to transactionId,
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
        val body = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "PDNO" to code, // 종목번호
            "ORD_DVSN" to "00", // 주문 구분 - 일단 지정가로 주문하기
            "ORD_QTY" to quantity, // 주문 수량
            "ORD_UNPR" to price, // 주문 단가
        )
        orderService.buy(headers, body)
    }

    override fun modifiable(accountNum: String) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "TTTC8036R", // 주식 정정 취소 가능 주문 조회
            "custtype" to "P",
        )

        val queries = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "CTX_AREA_FK100" to "", // 	연속조회검색조건100
                                    // 공란 : 최초 조회시
                                    //이전 조회 Output CTX_AREA_FK100 값 : 다음페이지 조회시(2번째부터)
            "CTX_AREA_NK100" to "", // 연속조회키100
                                    // 공란 : 최초 조회시
                                    // 이전 조회 Output CTX_AREA_NK100 값 : 다음페이지 조회시(2번째부터)
            "INQR_DVSN_1" to "1", // 조회구분1 0 : 조회순서 1 : 주문순 2 : 종목순
            "INQR_DVSN_2" to "0", // 조회구분2 0 : 전체 1 : 매도 2 : 매수
        )
        orderService.modifiable(headers, queries)
    }
}
