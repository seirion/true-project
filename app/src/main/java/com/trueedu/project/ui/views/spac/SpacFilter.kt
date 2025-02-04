package com.trueedu.project.ui.views.spac

data class SpacFilter(
    val listedOverTwoYears: Boolean = false, // 3년 차 종목
    val underParValue: Boolean = false, // 액면가 이하 가격 종목
)
