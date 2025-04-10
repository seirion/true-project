package com.trueedu.project.model.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ScheduleOrderCancelResponse(
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드 - "KIOK0560"
    @SerialName("msg")
    val msg: String, // 응답메세지
    val output: List<ResponseResult>,
)

@Serializable
data class ResponseResult(
    @SerialName("nrml_prcs_yn")
    val result: String, // Y or N
)
