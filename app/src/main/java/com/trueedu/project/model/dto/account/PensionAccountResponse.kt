package com.trueedu.project.model.dto.account

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PensionAccountResponse(
    val output1: List<PensionAsset>,
    val output2: PensionDetail,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드
    val msg1: String, // 응답메세지

    // 다음 연속 조회시 사용
    @SerialName("ctx_area_fk100")
    val fk100: String, // 연속조회검색조건100
    @SerialName("ctx_area_nk100")
    val nk100: String, // 연속조회키100
)

@Serializable
data class PensionAsset(
    @SerialName("pdno")
    val code: String, // 상품번호 (종목코드)
    @SerialName("prdt_name")
    val nameKr: String, // 상품명
    @SerialName("cblc_dvsn_name")
    val balanceDivisionName: String, // 잔고구분명
    @SerialName("item_dvsn_name")
    val itemDivisionName: String, // 종목구분명
    @SerialName("thdt_buyqty")
    val todayBuyQuantity: String, // 금일매수수량
    @SerialName("thdt_sll_qty")
    val todaySellQuantity: String, // 금일매도수량
    @SerialName("hldg_qty")
    val holdingQuantity: String, // 보유수량
    @SerialName("ord_psbl_qty")
    val orderPossibleQuantity: String, // 주문가능수량
    @SerialName("pchs_avg_pric")
    val purchaseAveragePrice: String, // 매입평균가격
    @SerialName("pchs_amt")
    val purchaseAmount: String, // 매입금액
    @SerialName("prpr")
    val currentPrice: String, // 현재가
    @SerialName("evlu_amt")
    val evaluationAmount: String, // 평가금액
    @SerialName("evlu_pfls_amt")
    val profitLossAmount: String, // 평가손익금액
    @SerialName("evlu_erng_rt")
    val evaluationEarningsRate: String, // 평가수익율
)

@Serializable
data class PensionDetail(
    @SerialName("dnca_tot_amt")
    val depositAccountTotalAmount: String, // 예수금총금액
    @SerialName("nxdy_excc_amt")
    val nextDayExcessAmount: String, // 익일정산금액 - D+1 예수금
    @SerialName("prvs_rcdl_excc_amt")
    val previousRedemptionExcessAmount: String, // 가수도정산금액 - D+2 예수금
    @SerialName("thdt_buy_amt")
    val todayBuyAmount: String, // 금일매수금액
    @SerialName("thdt_sll_amt")
    val todaySellAmount: String, // 금일매도금액
    @SerialName("thdt_tlex_amt")
    val todayTotalExpensesAmount: String, // 금일제비용금액
    @SerialName("scts_evlu_amt")
    val stockEvaluationAmount: String, // 유가평가금액
    @SerialName("tot_evlu_amt")
    val totalEvaluationAmount: String, // 총평가금액
) {
    // 총손익금액 — output2에 없으므로 output1 items에서 합산
    // 총수익률 — output1 items의 pchs_amt 합계 기준으로 계산
    fun totalProfitRate(items: List<PensionAsset>): Double {
        return try {
            val cost = items.sumOf { it.purchaseAmount.toDouble() }
            val value = items.sumOf { it.evaluationAmount.toDouble() }
            if (cost == 0.0) 0.0 else (value - cost) / cost * 100
        } catch (_: NumberFormatException) {
            0.0
        }
    }

    fun totalProfitAmount(items: List<PensionAsset>): Double {
        return try {
            items.sumOf { it.profitLossAmount.toDouble() }
        } catch (_: NumberFormatException) {
            0.0
        }
    }
}
