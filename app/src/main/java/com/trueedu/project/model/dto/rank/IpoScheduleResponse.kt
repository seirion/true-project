package com.trueedu.project.model.dto.rank

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IpoScheduleResponse(
    @SerialName("output1")
    val ipoScheduleDetail: List<IpoScheduleDetail>,
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "MCA00000"
    val msg1: String, // 응답메세지 - "정상처리 되었습니다."
)

@Serializable
data class IpoScheduleDetail(
    @SerialName("record_date")
    val recordDate: String, // 기준일
    @SerialName("sht_cd")
    val code: String, // 종목코드
    @SerialName("isin_name")
    val nameKr: String, // 종목명
    @SerialName("fix_subscr_pri")
    val fixSubscriptionPrice: String, // 공모가
    @SerialName("face_value")
    val faceValue: String, // 액면가
    @SerialName("subscr_dt")
    val subscriptionPeriod: String, // 청약기간
    @SerialName("pay_dt")
    val paymentDate: String, // 납입일
    @SerialName("refund_dt")
    val refundDate: String, // 환불일
    @SerialName("list_dt")
    val listingDate: String, // 상장/등록일
    @SerialName("lead_mgr")
    val leadManager: String, // 주간사
    @SerialName("pub_bf_cap")
    val beforeCapital: String, // 공모전자본금
    @SerialName("pub_af_cap")
    val afterCapital: String, // 공모후자본금
    @SerialName("assign_stk_qty")
    val assignedStockQuantity: String, // 당사배정물량
)
