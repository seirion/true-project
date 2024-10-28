package com.trueedu.project.ui.views.order

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

class OrderViewDrawer(
    private val vm: OrderViewModel,
): ComposableDrawer {
    @Composable
    override fun Draw() {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .weight(1f)
            ) {
                Section()
                OrderBook(vm.sells(), vm.buys(), vm.price(), vm.previousClose())
            }
            SellBuyButtons()
        }
    }
}
