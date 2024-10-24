package com.trueedu.project.ui.views.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.account.AccountOutput1
import com.trueedu.project.model.dto.account.AccountOutput2
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.widget.MyToggleButton
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter

@Composable
fun EmptyHome() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        BasicText(
            s = "자산 데이터 없음",
            fontSize = 18,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AccountInfo(
    accountInfo: AccountOutput2,
    dailyProfitMode: Boolean,
    onChangeDailyMode: (Int) -> Unit,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    val total = accountInfo.totalEvaluationAmount.toDouble()
    val totalString = formatter.format(total)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .padding(bottom = 8.dp)
    ) {
        val profit = accountInfo.profitLossSumTotalAmount.toDouble()
        val profitString = formatter.format(profit, true)

        val rate = accountInfo.totalProfitRate()
        val profitRateString = rateFormatter.format(rate, true)

        Column {
            BasicText(s = "Total", fontSize = 12, color = MaterialTheme.colorScheme.outline)
            // 총자산
            BasicText(s = totalString, fontSize = 24, color = MaterialTheme.colorScheme.primary)
            // 수익/수익률
            BasicText(
                s = "$profitString ($profitRateString)",
                fontSize = 14,
                color = ChartColor.color(profit)
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            MyToggleButton(
                defaultValue = if (dailyProfitMode) 0 else 1,
                textKeys = listOf( "시세", "수익"),
                toggleClick = onChangeDailyMode,
            )
        }
    }

    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        HeaderTitle("예수금")
        HeaderTitle("D+1 예수금")
        HeaderTitle("D+2 예수금")
    }
    Row(modifier = Modifier.padding(horizontal = 16.dp)) {
        BodyTitle(
            formatter.format(accountInfo.depositAccountTotalAmount.toDouble())
        )
        BodyTitle(
            formatter.format(accountInfo.nextDayExcessAmount.toDouble())
        )
        BodyTitle(
            formatter.format(accountInfo.previousRedemptionExcessAmount.toDouble())
        )
    }
    Margin(8)
}

@Composable
private fun HeaderTitle(s: String) {
    BasicText(
        s = s,
        fontSize = 12,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun RowScope.HeaderTitle(s: String) {
    BasicText(
        s = s,
        fontSize = 12,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun RowScope.BodyTitle(s: String) {
    BasicText(
        s = s,
        fontSize = 14,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.weight(1f)
    )
}

@Composable
fun StockItem(
    item: AccountOutput1,
    marketPriceMode: Boolean,
    onPriceClick: (String) -> Unit,
    onItemClick: (String) -> Unit,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onItemClick(item.code) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column {
            BasicText(
                s = item.nameKr,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )

            val subText = if (marketPriceMode) {
                "(${item.code})" // 종목 코드
            } else {
                "${item.holdingQuantity}주" // 수량
            }
            BasicText(
                s = subText,
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.clickable { onPriceClick(item.code) }
        ) {
            val totalValue = if (marketPriceMode) {
                // 시세 (현재 가격)
                item.currentPrice.toDouble()
            } else {
                // 평가금액
                item.evaluationAmount.toDouble()
            }
            val totalValueString = formatter.format(totalValue)
            val profit = if (marketPriceMode) {
                // 시세 변동
                item.priceChange.toDouble()
            } else {
                // 총수익 금액
                item.profitLossAmount.toDouble()
            }
            val profitString = formatter.format(profit, true)
            val profitRate = if (marketPriceMode) {
                item.priceChangeRate.toDouble()
            } else {
                item.profitLossRate.toDouble()
            }
            val profitRateString = rateFormatter.format(profitRate, true)
            BasicText(
                s = totalValueString,
                fontSize = 14,
                fontWeight = FontWeight.W600,
                color = ChartColor.color(profit),
            )
            BasicText(
                s = "$profitString ($profitRateString)",
                fontSize = 12,
                color = ChartColor.color(profit),
            )
        }
    }
}