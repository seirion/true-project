package com.trueedu.project.ui.views.home

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
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.account.AccountOutput1
import com.trueedu.project.model.dto.account.AccountOutput2
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.widget.MySwitch
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
    onChangeDailyMode: (Boolean) -> Unit,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    val total = accountInfo.totalEvaluationAmount.toDouble()
    val totalString = formatter.format(total)

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // 총자산
        Column {
            BasicText(s = "Total", fontSize = 12, color = MaterialTheme.colorScheme.outline)
            BasicText(s = totalString, fontSize = 24, color = MaterialTheme.colorScheme.primary)
        }

        Column(horizontalAlignment = Alignment.End) {
            val text = if (dailyProfitMode) "일간수익" else "총수익"
            BasicText(s = text, fontSize = 12, color = MaterialTheme.colorScheme.outline)
            MySwitch(
                checked = dailyProfitMode,
                onCheckedChange = onChangeDailyMode
            )
        }
    }

    val profit = if (dailyProfitMode) {
        accountInfo.assetChangeAmount.toDouble() // 일간 수익
    } else {
        accountInfo.profitLossSumTotalAmount.toDouble() // 총수익
    }
    val profitString = formatter.format(profit, true)

    val profitRateString = if (dailyProfitMode) {
        val dailyProfitRate = accountInfo.dailyProfitRate()
        rateFormatter.format(dailyProfitRate, true)
    } else {
        val totalProfitRate = accountInfo.totalProfitRate()
        rateFormatter.format(totalProfitRate, true)
    }
    BasicText(
        s = "$profitString ($profitRateString)",
        fontSize = 14,
        color = ChartColor.color(profit)
    )

    Row {
        HeaderTitle("예수금")
        HeaderTitle("D+1 예수금")
        HeaderTitle("D+2 예수금")
    }
    Row {
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
fun StockItem(item: AccountOutput1, dailyProfitMode: Boolean) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column {
            BasicText(
                s = item.nameKr,
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
            BasicText(
                s = "(${item.code})",
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            val totalValue = item.evaluationAmount.toDouble()
            val totalValueString = formatter.format(totalValue)
            BasicText(
                s = totalValueString,
                fontSize = 13,
                color = MaterialTheme.colorScheme.primary,
            )

            val profit = if (dailyProfitMode) {
                item.priceChange.toDouble() * item.holdingQuantity.toDouble()
            } else {
                item.profitLossAmount.toDouble()
            }
            val profitString = formatter.format(profit, true)
            val profitRate = if (dailyProfitMode) {
                item.priceChangeRate.toDouble()
            } else {
                item.profitLossRate.toDouble()
            }
            val profitRateString = rateFormatter.format(profitRate, true)
            BasicText(
                s = "$profitString ($profitRateString)",
                fontSize = 12,
                color = ChartColor.color(profit),
            )
        }
    }
}