package com.trueedu.project.model.dto.price

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IndexResponse(
    val output: IndexDetail?,
    @SerialName("rt_cd")
    val rtCd: String,
    @SerialName("msg_cd")
    val msgCd: String,
    val msg1: String,
)

@Serializable
data class IndexDetail(
    @SerialName("bstp_nmix_prpr")
    val price: String, // 업종 현재지수
    @SerialName("bstp_nmix_prdy_vrss")
    val priceChange: String, // 전일 대비
    @SerialName("prdy_vrss_sign")
    val priceChangeSign: String, // 전일 대비 부호 1:상한 2:상승 3:보합 4:하한 5:하락
    @SerialName("bstp_nmix_prdy_ctrt")
    val priceChangeRate: String, // 전일 대비율
    @SerialName("acml_vol")
    val volume: String, // 누적 거래량
    @SerialName("bstp_nmix_oprc")
    val open: String, // 시가
    @SerialName("bstp_nmix_hgpr")
    val high: String, // 고가
    @SerialName("bstp_nmix_lwpr")
    val low: String, // 저가
)
