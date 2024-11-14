package com.trueedu.project.ui.views.stock

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.price.DailyPrice
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.dateFormat

@Preview(showBackground = true)
@Composable
fun PriceCellView(
    title: String = "시가총액",
    value: String = "100억",
    color: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        TrueText(
            s = title,
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        TrueText(
            s = value,
            fontSize = 12,
            color = color,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
fun DailyPriceSection() {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceDim
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        listOf("날짜", "종가", "전일대비", "거래량").forEachIndexed { i, it ->
            val textAlign = if (i == 0) {
                TextAlign.Start
            } else {
                TextAlign.End
            }
            TrueText(
                s = it,
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = textAlign,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun DailyPriceCell(
    item: DailyPrice,
    bgColor: Color,
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
            .background(color = bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        listOf(
            item.date to ::dateFormat,
            item.close to ::numberFormat,
            item.change to ::numberFormat,
            item.volume to ::numberFormat,
        ).forEachIndexed { i, (s, formatting) ->
            val color = if (i == 0 || i == 3) {
               MaterialTheme.colorScheme.secondary
            } else {
                ChartColor.color(item.change!!.toDouble())
            }
            val textAlign = if (i == 0) {
                TextAlign.Start
            } else {
                TextAlign.End
            }
            TrueText(
                s = formatting(s ?: ""),
                fontSize = 12,
                color = color,
                textAlign = textAlign,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private fun numberFormat(s: String): String {
    return intFormatter.format(s.toDouble(), false)
}
