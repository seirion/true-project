package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.AccountService

class AccountRemoteImpl(
    @NormalService
    private val accountService: AccountService
): AccountRemote {

    override fun getUserStocks(
        accountNum: String,
        fk100: String,
        nk100: String,
    ) = apiCallFlow {
        val tc = if (fk100.isEmpty() || nk100.isEmpty()) "" else "N"
        val headers = mapOf(
            "tr_id" to "TTTC8434R", // 거래 ID - 주식 잔고 조회
            "tr_cont" to tc, // 연속 거래 여부 - 공백 : 초기 조회
                             // N : 다음 데이터 조회 (output header의 tr_cont가 M일 경우)
            //"custtype" to "P", // 개인
        )
        val queries = mapOf(
            "CANO" to accountNum.take(8), // 종합계좌번호
            "ACNT_PRDT_CD" to accountNum.drop(8), // 계좌상품코드
            "AFHR_FLPR_YN" to "N", // 시간외단일가여부
            "OFL_YN" to "", // 오프라인여부"
            "INQR_DVSN" to "02", // 조회구분 - 01 : 대출일별 02 : 종목별
            "UNPR_DVSN" to "01", // 단가구분
            "FUND_STTL_ICLD_YN" to "N", // 펀드결제분포함여부
            "FNCG_AMT_AUTO_RDPT_YN" to "N", // 융자금액자동상환여부
            "PRCS_DVSN" to "00", // 00 : 전일매매포함 01 : 전일매매미포함
            "CTX_AREA_FK100" to fk100, // 연속조회검색조건100	100	공란 : 최초 조회시
                     // CTX_AREA_FK100 이전 조회 Output CTX_AREA_FK100 값 : 다음페이지 조회시(2번째부터)
            "CTX_AREA_NK100" to nk100, // 연속조회키100	공란 : 최초 조회시
                    // 이전 조회 Output CTX_AREA_NK100 값 : 다음페이지 조회시(2번째부터)
        )
        accountService.getAccount(headers, queries)
    }
}
