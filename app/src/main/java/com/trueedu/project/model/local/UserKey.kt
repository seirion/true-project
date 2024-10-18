package com.trueedu.project.model.local

import kotlinx.serialization.Serializable

@Serializable
data class UserKey(
    val appKey: String?,
    val appSecret: String?,
    val accountNum: String?,
    val htsId: String?,
)
