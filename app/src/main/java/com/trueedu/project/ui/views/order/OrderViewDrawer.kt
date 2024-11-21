package com.trueedu.project.ui.views.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.project.base.ComposableDrawer
import com.trueedu.project.model.dto.account.AccountOutput1
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.widget.InputSet
import com.trueedu.project.utils.formatter.intFormatter

class OrderViewDrawer(
    private val vm: OrderViewModel,
    private val buy: () -> Unit,
    private val sell: () -> Unit,
): ComposableDrawer {
    @Composable
    override fun Draw() {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column {
                    Section()
                    OrderBook(vm.sells(), vm.buys(), vm.price(), vm.previousClose()) {
                        vm.setPrice(it)
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .padding(horizontal = 2.dp)
                ) {
                    InputSet("가격", vm.priceInput, vm::increasePrice, vm::decreasePrice)
                    Margin(24)
                    InputSet("수량", vm.quantityInput, vm::increaseQuantity, vm::decreaseQuantity)

                    val userAssets = vm.userAssets.assets.collectAsState(null)
                    val asset = userAssets.value?.output1?.let {
                        it.firstOrNull { it.code == vm.code }
                    }
                    if (asset != null) {
                        Margin(36)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Margin(24)
                        StockHoldingView(asset)
                    }
                }
            }
            SellBuyButtons(buy, sell)
        }
    }
}

@Composable
fun StockHoldingView(item: AccountOutput1) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        TrueText(
            s = "보유/주문가능",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
        TrueText(
            s = intFormatter.format(item.holdingQuantity.toDouble()) +
                    "/${intFormatter.format(item.orderPossibleQuantity.toDouble())}",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
