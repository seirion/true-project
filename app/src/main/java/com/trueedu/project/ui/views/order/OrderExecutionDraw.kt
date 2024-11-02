package com.trueedu.project.ui.views.order

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.trueedu.project.model.dto.price.OrderExecutionDetail
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.cashFormatter

class OrderExecutionDraw(
    private val vm: OrderExecutionViewModel,
): ComposableDrawer {

    @Composable
    override fun Draw() {
        if (vm.loading.value || vm.response.value == null) {
            LoadingView()
        } else {
            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize()
            ) {
                item { ExecutionListSection() }
                val items = vm.response.value!!.orderExecutionDetail ?: emptyList()
                itemsIndexed(items, key = { _, item -> item.orderNo }) { index, item ->
                    val bgColor = if (index % 2 == 0) {
                        MaterialTheme.colorScheme.background
                    } else {
                        MaterialTheme.colorScheme.surfaceDim
                    }
                    ItemView(item, bgColor) {}
                }
            }
        }
    }

    @Composable
    private fun ItemView(
        item: OrderExecutionDetail,
        bgColor: Color,
        onClick: () -> Unit,
    ) {
        val isBuy = item.sellBuyDivisionCode == "02"
        val primary = MaterialTheme.colorScheme.primary
        val chartColor = if (isBuy) ChartColor.up else ChartColor.down
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
                .background(color = bgColor)
                .clickable { onClick() }
                .padding(vertical = 4.dp)
        ) {
            val nameKr = item.nameKr

            Column(
                modifier = Modifier.weight(1f)
                    .padding(start = 4.dp),
                horizontalAlignment = Alignment.Start
            ) {
                TrueText(s = nameKr, fontSize = 13, fontWeight = FontWeight.W500, color = primary)
                TrueText(s = if (isBuy) "매수" else "매도", fontSize = 12, color = chartColor)
            }

            val price = cashFormatter.format(item.orderUnitPrice.toDouble())
            val quantity = cashFormatter.format(item.orderQuantity.toDouble())
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                TrueText(s = price, fontSize = 12, fontWeight = FontWeight.W500, color = primary)
                TrueText(s = quantity, fontSize = 12, color = primary)
            }

            val executionPrice = cashFormatter.format(item.averagePrice.toDouble())
            val executionQuantity = cashFormatter.format(item.totalConcludedQuantity.toDouble())
            Column(
                modifier = Modifier.weight(1f)
                    .padding(end = 4.dp),
                horizontalAlignment = Alignment.End
            ) {
                TrueText(s = executionPrice, fontSize = 12, fontWeight = FontWeight.W500, color = primary)
                TrueText(s = executionQuantity, fontSize = 12, color = primary)
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun ExecutionListSection() {
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val textList = listOf(
        "종목" to "구분",
        "주문단가" to "주문수량",
        "체결단가" to "체결수량",
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, borderColor)
            .padding(vertical = 4.dp),
    ) {
        val textColor = MaterialTheme.colorScheme.primary

        textList.forEach { (s0, s1) ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TrueText(
                    s = s0,
                    fontSize = 12,
                    color = textColor,
                )
                TrueText(
                    s = s1,
                    fontSize = 12,
                    color = textColor,
                )
            }
            VerticalDivider(thickness = 1.dp, color = borderColor)
        }
    }
}
