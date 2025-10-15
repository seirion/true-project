package com.trueedu.project.model.dto.firebase

import com.google.firebase.database.PropertyName

data class SpacStatus(
    val code: String,
    val nameKr: String,
    val redemptionPrice: Int?,
    @get:PropertyName("status")
    private val _status: Status?,
) {
    // No-argument constructor required for Firebase
    constructor() : this("000000", "", null, null)

    // status가 null인 경우 NORMAL 반환
    val status: Status
        get() = _status ?: Status.NORMAL

    enum class Status(val description: String) {
        NORMAL("일반"),
        MERGER_REVIEW("합병심사"),
        MERGER_APPROVED("합병승인"),
        DELISTING("상장폐지"),
        UNKNOWN("-"),
        ;
    }
}
