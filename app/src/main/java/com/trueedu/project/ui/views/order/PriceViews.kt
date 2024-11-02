package com.trueedu.project.ui.views.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.cashFormatter
import com.trueedu.project.utils.formatter.rateFormatter

@Composable
fun PriceViews(
    price: Double,
    priceChange: Double,
    rate: Double,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val priceString = cashFormatter.format(price, false)
        val changeString = cashFormatter.format(priceChange, true)
        val rateString = rateFormatter.format(rate, true)
        val textColor = ChartColor.color(priceChange)
        TrueText(
            s = priceString,
            fontSize = 24,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.End,
            color = textColor,
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
        )
        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.weight(1f)
                .fillMaxWidth()
        ) {
            TrueText(s = changeString, fontSize = 14, color = textColor)
            TrueText(s = rateString, fontSize = 12, color = textColor)
        }
    }
}
