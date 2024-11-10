package com.trueedu.project.ui.views.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.trueedu.project.base.ComposableDrawer
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.widget.InputSet

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
                }
            }
            SellBuyButtons(buy, sell)
        }
    }
}
