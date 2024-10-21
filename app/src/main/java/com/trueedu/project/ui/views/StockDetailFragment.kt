package com.trueedu.project.ui.views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackStockTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StockDetailFragment: BaseFragment() {
    companion object {
        fun show(
            stockInfo: StockInfo,
            fragmentManager: FragmentManager
        ): StockDetailFragment {
            val fragment = StockDetailFragment()
            fragment.stockInfo = stockInfo
            fragment.show(fragmentManager, "stock-detail")
            return fragment
        }
    }

    lateinit var stockInfo: StockInfo

    private val vm by viewModels<StockDetailViewModel>()

    override fun onStart() {
        super.onStart()
        if (!::stockInfo.isInitialized) {
            dismissAllowingStateLoss()
        }
        // push
    }

    override fun onStop() {
        super.onStop()
        // pop
    }

    override fun init() {
        vm.init(stockInfo)
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.destroy()
    }

    /**
     * return [priceString, textColor]
     */
    private fun priceChangeStr(): Pair<String, Color> {
        val code = stockInfo.code
        val priceInfo = vm.priceManager.dataMap[code]
        val priceChange = priceInfo?.delta
        val rate = priceInfo?.rate
        if (priceChange != null && rate != null) {
            val formatter = CashFormatter()
            val rateFormatter = RateFormatter()
            return "${formatter.format(priceChange, true)} (" +
                    rateFormatter.format(rate, true) +
                    ")" to ChartColor.color(priceChange)
        } else {
            return "" to ChartColor.up
        }
    }

    @Composable
    override fun BodyScreen() {
        if (!::stockInfo.isInitialized) dismissAllowingStateLoss()
        Scaffold(
            topBar = {
                val (priceChangeStr, textColor) = priceChangeStr()
                BackStockTopBar(
                    stockInfo.nameKr,
                    priceChangeStr,
                    textColor,
                    ::dismissAllowingStateLoss
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                vm.infoList.value.forEach {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        BasicText(s = it.first, fontSize = 16, color = MaterialTheme.colorScheme.primary)
                        BasicText(s = it.second ?: "", fontSize = 16, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
