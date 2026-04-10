package com.trueedu.project.model.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PensionFundResponse(
    val output1: List<PensionFundAsset>,
    val output2: List<PensionFundDetail>,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드
    val msg1: String, // 응답메세지

    // 다음 연속 조회시 사용
    @SerialName("ctx_area_fk100")
    val fk100: String,
    @SerialName("ctx_area_nk100")
    val nk100: String,
)

@Serializable
data class PensionFundAsset(
    @SerialName("cblc_dvsn")
    val balanceDivision: String, // 잔고구분
    @SerialName("cblc_dvsn_name")
    val balanceDivisionName: String, // 잔고구분명
    @SerialName("pdno")
    val code: String, // 상품번호 (펀드코드)
    @SerialName("prdt_name")
    val nameKr: String, // 상품명
    @SerialName("hldg_qty")
    val holdingQuantity: String, // 보유수량
    @SerialName("slpsb_qty")
    val sellPossibleQuantity: String, // 매도가능수량
    @SerialName("pchs_avg_pric")
    val purchaseAveragePrice: String, // 매입평균가격
    @SerialName("evlu_pfls_amt")
    val profitLossAmount: String, // 평가손익금액
    @SerialName("evlu_pfls_rt")
    val profitLossRate: String, // 평가손익율
    @SerialName("prpr")
    val currentPrice: String, // 현재가 (기준가)
    @SerialName("evlu_amt")
    val evaluationAmount: String, // 평가금액
    @SerialName("pchs_amt")
    val purchaseAmount: String, // 매입금액
    @SerialName("cblc_weit")
    val balanceWeight: String, // 잔고비중
)

@Serializable
data class PensionFundDetail(
    @SerialName("pchs_amt_smtl_amt")
    val purchaseAmountSumTotalAmount: String, // 매입금액합계금액
    @SerialName("evlu_amt_smtl_amt")
    val evaluationAmountSumTotalAmount: String, // 평가금액합계금액
    @SerialName("evlu_pfls_smtl_amt")
    val profitLossSumTotalAmount: String, // 평가손익합계금액
    @SerialName("trad_pfls_smtl")
    val tradeProfitLossSum: String, // 매매손익합계
    @SerialName("thdt_tot_pfls_amt")
    val todayTotalProfitLossAmount: String, // 당일총손익금액
    @SerialName("pftrt")
    val profitRate: String, // 수익률
)
