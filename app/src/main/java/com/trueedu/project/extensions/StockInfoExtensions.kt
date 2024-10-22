package com.trueedu.project.extensions

import androidx.compose.ui.graphics.Color
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter

private val cashFormatter = CashFormatter()
private val rateFormatter = RateFormatter()

/**
 * return [priceString, textColor]
 */
fun priceChangeStr(priceInfo: RealTimeTrade?): Pair<String, Color> {
    val priceChange = priceInfo?.delta
    val rate = priceInfo?.rate
    if (priceChange != null && rate != null) {
        return "${cashFormatter.format(priceChange, true)} (" +
                rateFormatter.format(rate, true) +
                ")" to ChartColor.color(priceChange)
    } else {
        return "" to ChartColor.up
    }
}

