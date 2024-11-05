package com.trueedu.project.ui.views.watch

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WatchingStockItem(
    nameKr: String,
    code: String,
    price: Double,
    delta: Double,
    rate: Double,
    volume: Double,
    onTradingClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            TrueText(
                s = nameKr,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
            TrueText(
                s = "(${code})",
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier
                .clickable { onTradingClick() }
        ) {
            val totalValueString = formatter.format(price)
            TrueText(
                s = totalValueString,
                fontSize = 14,
                fontWeight = FontWeight.W600,
                color = ChartColor.color(delta),
            )

            val profitString = formatter.format(delta, true)
            val profitRateString = rateFormatter.format(rate, true)
            TrueText(
                s = "$profitString ($profitRateString)",
                fontSize = 12,
                color = ChartColor.color(delta),
            )
        }
    }
}
