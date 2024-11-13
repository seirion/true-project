package com.trueedu.project.ui.views.spac

data class SpacProfit(
    val totalCost: Double,
    val totalValue: Double,
) {
    companion object {
        val empty = SpacProfit(0.0, 0.0)
    }
}
