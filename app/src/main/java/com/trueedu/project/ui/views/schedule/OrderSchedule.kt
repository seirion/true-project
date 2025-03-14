package com.trueedu.project.ui.views.schedule

import kotlinx.serialization.Serializable

@Serializable
data class OrderSchedule(
    val code: String,
    val isBuy: Boolean,
    val price: Int,
    val quantity: Int,
)
