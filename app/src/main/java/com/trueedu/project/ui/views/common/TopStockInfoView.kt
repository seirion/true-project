package com.trueedu.project.ui.views.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.price.StockDetail
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.stock.PriceCellView
import com.trueedu.project.utils.formatter.cashFormatter
import com.trueedu.project.utils.formatter.rateFormatter

/**
 * 가격    | 시가
 * 변동(%) | 고가
 * 거래량  | 저가
 */
@Composable
fun TopStockInfoView(stockDetail: StockDetail) {
    val priceString = cashFormatter.format(
        stockDetail.price.toDouble()
    )
    val priceChange = stockDetail.priceChange.toDouble()
    val priceChangeRate = stockDetail.priceChangeRate.toDouble()
    val volume = stockDetail.volume.toDouble()
    val textColor = ChartColor.color(priceChange)

    Row(
        modifier = Modifier.fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(
            //horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f),
        ) {
            // 현재 가격
            BasicText(
                s = priceString,
                fontSize = 24,
                fontWeight = FontWeight.W500,
                color = textColor,
            )
            // 전일 대비
            BasicText(
                s = "${cashFormatter.format(priceChange, false)} " +
                        "(${rateFormatter.format(priceChangeRate)})",
                fontSize = 12,
                color = ChartColor.color(priceChange)
            )
            // 거래량
            BasicText(
                s = "${cashFormatter.format(volume, false)}",
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Margin(16)
        Column(
            modifier = Modifier.weight(0.8f),
        ) {
            val basePrice = stockDetail.previousPrice.toDouble()
            listOf(
                "시가" to stockDetail.`open`.toDouble(),
                "고가" to stockDetail.high.toDouble(),
                "저가" to stockDetail.low.toDouble(),
            ).forEach { (title, value) ->
                val color = ChartColor.color(value - basePrice)
                val valueString = cashFormatter.format(value)
                PriceCellView(title, valueString, color)
            }
        }
    }
}
