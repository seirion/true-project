package com.trueedu.project.ui.views.order

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.base.ComposableDrawer
import com.trueedu.project.data.UserAssets
import com.trueedu.project.model.dto.account.AccountOutput1
import com.trueedu.project.model.dto.account.AccountResponse
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter
import com.trueedu.project.utils.formatter.cashFormatter
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BalanceDrawer(
    private val userAssets: UserAssets,
    private val onStockClick: (String) -> Unit,
): ComposableDrawer {
    private val userStocks = mutableStateOf<AccountResponse?>(null)

    init {
        MainScope().launch {
            launch {
                userAssets.assets.collectLatest {
                    userStocks.value = it
                }
            }
        }
    }

    fun init() {
        // reload
        userAssets.loadUserStocks()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Draw() {
        if (userStocks.value == null) {
            LoadingView()
        } else {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                stickyHeader { BalanceSection() }
                val items = userStocks.value!!.output1
                    .filter { it.holdingQuantity.toDouble() > 0 }
                itemsIndexed(items, { _, item -> item.code} ) { i, item ->
                    ItemView(i, item, onStockClick)
                }
            }
        }
    }

    @Composable
    private fun ItemView(
        index: Int,
        item: AccountOutput1,
        onClick: (String) -> Unit,
    ) {
        val formatter = CashFormatter()
        val rateFormatter = RateFormatter()
        val bgColor = if (index % 2 == 0) {
            MaterialTheme.colorScheme.background
        } else {
            MaterialTheme.colorScheme.surfaceDim
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .background(color = bgColor)
                .clickable { onClick(item.code) }
                .padding(horizontal = 2.dp, vertical = 4.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TrueText(
                    s = item.nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                val quantity = cashFormatter.format(item.holdingQuantity.toDouble())
                TrueText(
                    s = "${quantity}주",
                    fontSize = 12,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                val totalValue = item.evaluationAmount.toDouble() // 평가금액
                val totalValueString = formatter.format(totalValue)
                val profit = item.profitLossAmount.toDouble() // 총수익 금액
                val profitString = formatter.format(profit, true)
                val profitRate = item.profitLossRate.toDouble()
                val profitRateString = rateFormatter.format(profitRate, true)

                TrueText(
                    s = totalValueString,
                    fontSize = 14,
                    fontWeight = FontWeight.W500,
                    color = ChartColor.color(profit),
                )
                TrueText(
                    s = "$profitString ($profitRateString)",
                    fontSize = 12,
                    color = ChartColor.color(profit),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BalanceSection() {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val bgColor = MaterialTheme.colorScheme.background
    val textList = listOf(
        "종목/잔고수량" to 1f,
        "수익/수익률" to 1f,
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(24.dp)
            .background(color = bgColor)
            .border(width = 1.dp, borderColor)
            .padding(vertical = 2.dp),
    ) {
        val textColor = MaterialTheme.colorScheme.primary

        textList.forEach { (s, w) ->
            TrueText(
                s =  s,
                fontSize = 12,
                color = textColor,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(w)
            )
            VerticalDivider(thickness = 1.dp, color = borderColor)
        }
        Margin(1)
    }
}
