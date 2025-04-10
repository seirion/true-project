package com.trueedu.project.ui.views.order

import androidx.compose.foundation.clickable
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
import com.trueedu.project.model.dto.account.AccountAsset
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.price.OrderModifiableDetail
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.widget.InputSet
import com.trueedu.project.utils.formatter.intFormatter
import com.trueedu.project.utils.formatter.numberFormatString
import com.trueedu.project.utils.formatter.safeDouble

class OrderViewDrawer(
    private val vm: OrderViewModel,
    private val modifyVm: OrderModifyViewModel,
    private val buy: () -> Unit,
    private val sell: () -> Unit,
    private val modify: (OrderModifiableDetail) -> Unit, // original order
    private val setOrderQuantity: (Double) -> Unit,
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
                    val myBuySells = modifyVm.items.value
                        ?.orderModifiableDetail
                        ?.filter { vm.code == it.code }
                        ?.map { it.price.safeDouble() to it.possibleQuantity.safeDouble() }
                        ?.groupBy { it.first }
                        ?.mapValues { it.value.sumOf { it.second } }
                        ?: emptyMap()

                    OrderBook(
                        vm.sells(),
                        vm.buys(),
                        vm.price(),
                        vm.previousClose(),
                        myBuySells,
                    ) {
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
                    Margin(36)
                    if (asset != null) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        StockHoldingView(asset, setOrderQuantity)
                    }
                    vm.stockInfo()?.let { MarketCapView(it) }
                }
            }

            if (vm.originalOrder.value == null) {
                SellBuyButtons(buy, sell)
            } else {
                // 01 매도, 02 매수
                ModifyButtons(vm.originalOrder.value!!.sellBuyDivisionCode == "02") {
                    modify(vm.originalOrder.value!!)
                }
            }
        }
    }
}

@Composable
fun StockHoldingView(
    item: AccountAsset,
    onClick: (Double) -> Unit,
) {
    val orderPossibleQuantity = item.orderPossibleQuantity.toDouble()
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { onClick(orderPossibleQuantity) }
            .padding(vertical = 12.dp)
    ) {
        TrueText(
            s = "보유/매도가능",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
        TrueText(
            s = intFormatter.format(item.holdingQuantity.toDouble()) +
                    "/${intFormatter.format(orderPossibleQuantity)}",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun MarketCapView(stock: StockInfo) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            //.padding(vertical = 12.dp)
    ) {
        TrueText(
            s = "시가총액",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
        TrueText(
            s = numberFormatString(stock.marketCap()) + "억",
            fontSize = 12,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
