package com.trueedu.project.model.dto

data class StockInfo(
    val code: String,
    val nameKr: String,
    val attributes: String,
) {
    companion object {
        fun from(str: String): StockInfo {
            val code = str.substring(0, 9).trim()
            val nameKr = str.substring(21, str.length - 228).trim()
            val attributes = str.takeLast(228)

            return StockInfo(code, nameKr, attributes)
        }
    }

    // No-argument constructor required for Firebase
    constructor() : this("000000", "", "")
}
