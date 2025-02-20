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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.account.AccountAsset
import com.trueedu.project.model.dto.account.AccountDetail
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TouchIconWithSizeRotating
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.common.DesignatedBadge
import com.trueedu.project.ui.views.common.HaltBadge
import com.trueedu.project.ui.widget.MyToggleButton
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter
import com.trueedu.project.utils.formatter.intFormatter

@Composable
fun EmptyHome() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        TrueText(
            s = "자산 데이터 없음",
            fontSize = 18,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
fun AccountInfo(
    accountInfo: AccountDetail,
    dailyProfitMode: Boolean,
    onRefresh: () -> Unit,
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
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    ) {
        val profit = accountInfo.profitLossSumTotalAmount.toDouble()
        val profitString = formatter.format(profit, true)

        val rate = accountInfo.totalProfitRate()
        val profitRateString = rateFormatter.format(rate, true)

        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 총자산
                TrueText(
                    s = totalString,
                    fontSize = 24,
                    fontWeight = FontWeight.W500,
                    color = MaterialTheme.colorScheme.primary
                )
                // 새로 고침 버튼
                TouchIconWithSizeRotating(
                    size = 24.dp,
                    tint= MaterialTheme.colorScheme.primary,
                    icon = Icons.Outlined.Sync,
                    onClick = onRefresh
                )
            }
            // 수익/수익률
            TrueText(
                s = "$profitString ($profitRateString)",
                fontSize = 14,
                color = ChartColor.color(profit)
            )
        }

        MyToggleButton(
            defaultValue = if (dailyProfitMode) 0 else 1,
            textKeys = listOf( "시세", "수익"),
            toggleClick = onChangeDailyMode,
        )
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
    TrueText(
        s = s,
        fontSize = 12,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun RowScope.HeaderTitle(s: String) {
    TrueText(
        s = s,
        fontSize = 12,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun RowScope.BodyTitle(s: String) {
    TrueText(
        s = s,
        fontSize = 14,
        color = MaterialTheme.colorScheme.secondary,
        modifier = Modifier.weight(1f)
    )
}

@Composable
fun HomeStockItem(
    item: AccountAsset,
    stock: StockInfo?,
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
            .padding(8.dp)
    ) {
        Column {
            Row {
                TrueText(
                    s = item.nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                if (stock?.halt() == true) {
                    Margin(2)
                    HaltBadge()
                }
                if (stock?.designated() == true) {
                    Margin(2)
                    DesignatedBadge()
                }
            }

            val priceString = intFormatter.format(item.purchaseAveragePrice.toDouble())
            TrueText(
                s = "${priceString}원 • ${item.holdingQuantity}주",
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
                try {
                    item.priceChangeRate.toDouble()
                } catch (e: NumberFormatException) {
                    0.0
                }
            } else {
                item.profitLossRate.toDouble()
            }
            val profitRateString = rateFormatter.format(profitRate, true)
            val totalValueColor = if (marketPriceMode) {
                ChartColor.color(profit)
            } else {
                MaterialTheme.colorScheme.primary
            }
            TrueText(
                s = totalValueString,
                fontSize = 14,
                fontWeight = FontWeight.W600,
                color = totalValueColor,
            )
            TrueText(
                s = "$profitString ($profitRateString)",
                fontSize = 12,
                color = ChartColor.color(profit),
            )
        }
    }
}