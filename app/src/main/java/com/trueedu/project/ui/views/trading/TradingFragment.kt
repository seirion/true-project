package com.trueedu.project.ui.views.trading

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.theme.ChartColor
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TradingFragment: BaseFragment() {
    companion object {
        fun show(
            code: String,
            fragmentManager: FragmentManager
        ): TradingFragment {
            val fragment = TradingFragment()
            fragment.code = code
            fragment.show(fragmentManager, "trading")
            return fragment
        }
    }

    lateinit var code: String
    private val vm by viewModels<TradingViewModel>()

    override fun init() {
        super.init()
        vm.init(code)
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.destroy()
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                val stockName = vm.stockInfo()?.nameKr ?: ""
                BackTitleTopBar(stockName, ::dismissAllowingStateLoss)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 2.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                        .fillMaxWidth()
                ) {
                    PriceViews(
                        price = vm.price(),
                        priceChange = vm.priceChange(),
                        rate = vm.priceChangeRate(),
                    )
                    Section()
                    OrderBook(vm.sells(), vm.buys(), vm.previousClose())
                }
                SellBuyButtons()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SellBuyButtons() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(52.dp)
            .padding(horizontal = 16.dp)
    ) {
        val up = ChartColor.up
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
                .fillMaxHeight(),
            colors = ButtonColors(
                containerColor = up,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        ) {
            BasicText(
                s = "매수",
                fontSize = 18,
                color = MaterialTheme.colorScheme.background,
            )
        }
        Margin(8)
        val down = ChartColor.down
        Button(
            onClick = { /* TODO */ },
            modifier = Modifier.weight(1f)
                .fillMaxHeight(),
            colors = ButtonColors(
                containerColor = down,
                contentColor = MaterialTheme.colorScheme.background,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = Color.Transparent,
            ),
        ) {
            BasicText(
                s = "매도",
                fontSize = 18,
                color = MaterialTheme.colorScheme.background,
            )
        }
    }
}
