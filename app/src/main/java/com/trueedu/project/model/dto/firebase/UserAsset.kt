package com.trueedu.project.model.dto.firebase

/**
 * firebase database 에 보유 자산을 저장하기 위한 DTO
 */
class UserAsset(
    val code: String,
    val nameKr: String,
    val price: Double,
    val quantity: Double,
)
