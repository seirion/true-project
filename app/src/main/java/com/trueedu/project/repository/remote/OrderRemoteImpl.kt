package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.OrderService
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

    override fun modify(
        accountNum: String,
        originalOrderCode: String,
        priceString: String,
        quantityString: String,
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "TTTC0803U", // 주식 정정 취소 주문
            "custtype" to "P",
        )
        val body = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "KRX_FWDG_ORD_ORGNO" to "", // 주문시 한국투자증권 시스템에서 지정된 영업점코드
            "ORGN_ODNO" to originalOrderCode, // 원주문번호
            "ORD_DVSN" to "00", // 주문 구분 - 일단 지정가로 주문하기
            "RVSE_CNCL_DVSN_CD" to "01", // 정정 : 01 취소 : 02

            /**
             * [잔량전부 취소/정정주문]
             * "0" 설정 ( QTY_ALL_ORD_YN=Y 설정 )
             *
             * [잔량일부 취소/정정주문]
             * 취소/정정 수량
             */
            "ORD_QTY" to quantityString, // 주문 수량
            /**
             * [정정]
             * (지정가) 정정주문 1주당 가격
             * (시장가) "0" 설정
             *
             * [취소]
             * "0" 설정
             */
            "ORD_UNPR" to priceString, // 주문 단가
            "QTY_ALL_ORD_YN" to "Y", // 잔량전부주문여부 - Y : 잔량전부 N : 잔량일부
        )
        orderService.modify(headers, body)
    }

    override fun cancel(
        accountNum: String,
        originalOrderCode: String,
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "TTTC0803U", // 주식 정정 취소 주문
            "custtype" to "P",
        )
        val body = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "KRX_FWDG_ORD_ORGNO" to "", // 주문시 한국투자증권 시스템에서 지정된 영업점코드
            "ORGN_ODNO" to originalOrderCode, // 원주문번호
            "ORD_DVSN" to "00", // 주문 구분 - 일단 지정가로 주문하기
            "RVSE_CNCL_DVSN_CD" to "02", // 정정 : 01 취소 : 02

            /**
             * [잔량전부 취소/정정주문]
             * "0" 설정 ( QTY_ALL_ORD_YN=Y 설정 )
             *
             * [잔량일부 취소/정정주문]
             * 취소/정정 수량
             */
            "ORD_QTY" to "0", // 주문 수량
            /**
             * [정정]
             * (지정가) 정정주문 1주당 가격
             * (시장가) "0" 설정
             *
             * [취소]
             * "0" 설정
             */
            "ORD_UNPR" to "0", // 주문 단가
            "QTY_ALL_ORD_YN" to "Y", // 잔량전부주문여부 - Y : 잔량전부 N : 잔량일부
        )
        orderService.modify(headers, body)
    }


    override fun orderExecution(
        accountNum: String,
        code: String,
        fromDate: String,
        toDate: String,
    ) = apiCallFlow {
        // TTTC8001R : 주식 일별 주문 체결 조회(3개월이내)
        // CTSC9115R : 주식 일별 주문 체결 조회(3개월이전)
        val headers = mapOf(
            "tr_id" to "TTTC8001R",
            "custtype" to "P",
        )
        val queries = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "INQR_STRT_DT" to fromDate, // 조회 시작 일자
            "INQR_END_DT" to toDate, // 조회 종료 일자
            "SLL_BUY_DVSN_CD" to "00", // 00 : 전체 01 : 매도 02 : 매수
            "INQR_DVSN" to "00", // 조회 구분 - 00 : 역순 01 : 정순
            "PDNO" to "", // 종목번호 - empty 이면 ?
            "CCLD_DVSN" to "01", // 체결 구분 - 00 : 전체 01 : 체결 02 : 미체결
            "ORD_GNO_BRNO" to "", // 주문채번지점번호(empty)
            "ODNO" to "", // 주문번호 - 조회기간이 2일 이상인 경우, 공란 입력
                          // - 조회기간이 1일인 경우에만 주문번호(ODNO)로 조회 가능
            "INQR_DVSN_3" to "00", // 조회구분3 - 00 : 전체 01 : 현금 02 : 융자 03 : 대출 04 : 대주
            "INQR_DVSN_1" to "", // 조회구분1 - 공란 : 전체 1 : ELW 2 : 프리보드
            "CTX_AREA_FK100" to "", // 연속조회검색조건100
            "CTX_AREA_NK100" to "", // 연속조회키100
        )
        orderService.orderExecution(headers, queries)
    }

    override fun scheduleOrderList(
        accountNum: String,
        fk200: String,
        nk200: String,
    ) = apiCallFlow {
        val tc = if (fk200.isEmpty() || nk200.isEmpty()) "" else "N"
        val headers = mapOf(
            "tr_id" to "CTSC0004R", // 국내주식예약주문조회
            "custtype" to "P",
            "tr_cont" to tc,
        )
        val fromDate = LocalDate.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val toDate = LocalDate.now().plusMonths(1)
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"))
        val queries = mapOf(
            "RSVN_ORD_ORD_DT" to fromDate, // 예약주문시작일자(8)
            "RSVN_ORD_END_DT" to toDate, // 예약주문종료일자(8)
            "RSVN_ORD_SEQ" to "", // 예약주문순번(10)
            "TMNL_MDIA_KIND_CD" to "00", // 단말매체종류코드 - "00" 입력

            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "PRCS_DVSN_CD" to "0", // 처리구분코드	0: 전체 1: 처리내역 2: 미처리내역
            "CNCL_YN" to "Y", // 취소여부	"Y" 유효한 주문만 조회
            "PDNO" to "", // 종목코드(6자리) (공백 입력 시 전체 조회)
            "SLL_BUY_DVSN_CD" to "", // 매도매수구분코드 00 : 전체 / 01 : 매도 / 02 : 매수 (공백이 전체임)
            "CTX_AREA_FK200" to fk200, // 연속조회검색조건200	다음 페이지 조회시 사용
            "CTX_AREA_NK200" to nk200, // 연속조회키200	다음 페이지 조회시 사용
        )
        orderService.scheduleOrderResult(headers, queries)
    }

    override fun scheduleOrder(
        accountNum: String,
        code: String,
        isBuy: Boolean,
        price: String,
        quantity: String,
        endDate: String,
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "CTSC0008U", // 국내예약매수입력/주문예약매도입력
            "custtype" to "P",
        )
        val body = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "PDNO" to code, // 종목번호
            "ORD_QTY" to quantity, // 주문 수량
            "ORD_UNPR" to price, // 주문 단가
            "SLL_BUY_DVSN_CD" to if(isBuy) "02" else "01", // 01 : 매도 02 : 매수
            "ORD_DVSN_CD" to "00", // 00 : 지정가 01 : 시장가 02 : 조건부지정가 05 : 장전 시간외
            "ORD_OBJT_CBLC_DVSN_CD" to "10", // 주문대상잔고구분코드: 항상 '현금'만 사용
            "RSVN_ORD_END_DT" to endDate, // yyyyMMdd, 값이 없으면 다음날 주문처리되고 예약주문은 종료됨
        )
        orderService.scheduleOrder(headers, body)
    }

    override fun cancelScheduleOrder(
        accountNum: String,
        orderSeq: String,
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "CTSC0009U", // CTSC0009U(취소), CTSC0013U(정정)
            "custtype" to "P",
        )
        val body = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "RSVN_ORD_SEQ" to orderSeq, // 예약주문순번
        )
        orderService.cancelScheduleOrder(headers, body)
    }

    override fun modifyScheduleOrder(
        accountNum: String,
        code: String,
        orderSeq: String,
        isBuy: Boolean,
        price: String,
        quantity: String,
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "CTSC0013U", // CTSC0009U(취소), CTSC0013U(정정)
            "custtype" to "P",
        )
        val body = mapOf(
            "CANO" to accountNum.take(8), // 계좌번호 체계(8-2)의 앞 8자리
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌번호 체계(8-2)의 뒤 2자리
            "PDNO" to code,
            "ORD_QTY" to quantity, // 주문 수량
            "ORD_UNPR" to price, // 주문 단가
            "SLL_BUY_DVSN_CD" to if(isBuy) "02" else "01", // 01 : 매도 02 : 매수
            "ORD_DVSN_CD" to "00", // 00 : 지정가 01 : 시장가 02 : 조건부지정가 05 : 장전 시간외
            "ORD_OBJT_CBLC_DVSN_CD" to "10", // 주문대상잔고구분코드: 항상 '현금'만 사용
            "RSVN_ORD_SEQ" to orderSeq, // 예약주문순번
        )
        orderService.modifyScheduleOrder(headers, body)
    }
}
