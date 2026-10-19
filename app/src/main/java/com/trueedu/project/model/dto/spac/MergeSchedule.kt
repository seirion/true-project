package com.trueedu.project.model.dto.spac

import kotlinx.serialization.Serializable

/**
 * 스팩 합병 일정 (true-education.github.io data/merge.txt, JSON 배열)
 */
@Serializable
data class MergeSchedule(
    val nameKr: String = "",
    val code: String = "",
    val target: String = "", // 합병 대상
    // 합병반대의사통지 접수기간 (yyyyMMdd)
    val dissentNoticeStartDate: String = "",
    val dissentNoticeEndDate: String = "",
    // 주식매수청구권 행사기간 (yyyyMMdd)
    val appraisalRightStartDate: String = "",
    val appraisalRightEndDate: String = "",
    // 매매거래 정지예정기간 (yyyyMMdd)
    val tradingHaltStartDate: String = "",
    val tradingHaltEndDate: String = "",
    // 신주의 상장예정일 (yyyyMMdd)
    val newShareListingDate: String = "",
    // 공시 URL
    val disclosureUrl: String = "",
)
