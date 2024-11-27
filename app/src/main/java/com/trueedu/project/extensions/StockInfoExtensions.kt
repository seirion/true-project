package com.trueedu.project.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.model.ws.RealTimeTrade
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter

private val cashFormatter = CashFormatter()
private val rateFormatter = RateFormatter()

/**
 * return [priceString, textColor]
 */
@Composable
fun priceChangeStr(priceInfo: RealTimeTrade?): Pair<String, Color> {
    val priceChange = priceInfo?.delta
    val rate = priceInfo?.rate
    return priceChangeStr(priceChange, rate)
}

@Composable
fun priceChangeStr(priceResponse: PriceResponse): Pair<String, Color> {
    val priceChange = priceResponse.output!!.priceChange.toDouble()
    val rate = priceResponse.output.priceChangeRate.toDouble()
    return priceChangeStr(priceChange, rate)
}

@Composable
private fun priceChangeStr(priceChange: Double?, rate: Double?): Pair<String, Color> {
    if (priceChange != null && rate != null) {
        return "${cashFormatter.format(priceChange, true)} (" +
                rateFormatter.format(rate, true) +
                ")" to ChartColor.color(priceChange)
    } else {
        return "" to ChartColor.up
    }
}
