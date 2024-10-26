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
import com.trueedu.project.ui.common.BasicText

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
        BasicText(
            s = title,
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        BasicText(
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
        listOf("날짜", "종가", "전일대비", "거래량").forEach {
            BasicText(
                s = it,
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
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
            item.date,
            item.close,
            item.change,
            item.volume,
        ).forEach {
            BasicText(
                s = it,
                fontSize = 12,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f),
            )
        }
    }
}