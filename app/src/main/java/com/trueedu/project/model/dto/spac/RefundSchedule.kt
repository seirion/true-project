package com.trueedu.project.model.dto.spac

import kotlinx.serialization.Serializable

/**
 * 스팩 청산 일정 (true-education.github.io data/refund.json)
 * JSON 포맷: { "종목코드": { code, nameKr, refundAmount, date, fixed }, ... }
 */
@Serializable
data class RefundSchedule(
    val nameKr: String = "",
    val code: String = "",
    val date: String = "", // yyyyMMdd (입금일)
    val refundAmount: Double? = null,
    val fixed: Boolean = false,
)
