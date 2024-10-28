package com.trueedu.project.ui.views.order

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import com.trueedu.project.ui.views.common.TopStockInfoView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderFragment: BaseFragment() {
    companion object {
        fun show(
            code: String,
            fragmentManager: FragmentManager
        ): OrderFragment {
            val fragment = OrderFragment()
            fragment.code = code
            fragment.show(fragmentManager, "trading")
            return fragment
        }
    }

    lateinit var code: String
    private val vm by viewModels<OrderViewModel>()
    private val modifyVm by viewModels<OrderModifyViewModel>()

    @Inject
    lateinit var local: Local

    private lateinit var orderViewDrawer: OrderViewDrawer
    private lateinit var modifiableViewDrawer: ModifiableViewDrawer

    private val currentTab = mutableStateOf(OrderTab.Order)

    private fun setOrderTab(tab: OrderTab) {
        currentTab.value = tab
        local.setOrderTab(tab)
    }

    override fun init() {
        super.init()
        orderViewDrawer = OrderViewDrawer(vm)
        modifiableViewDrawer = ModifiableViewDrawer(modifyVm)

        //currentTab.value = local.getOrderTab()
        vm.init(code)

        lifecycleScope.launch {
            snapshotFlow { currentTab.value }
                .collect {
                    if (it == OrderTab.Modification) {
                        modifyVm.init()
                    }
                }
        }
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
                TopStockInfoViewInternal()
                TabViews()

                when (currentTab.value) {
                    OrderTab.Order -> {
                        orderViewDrawer.Draw()
                    }
                    OrderTab.Modification -> {
                        modifiableViewDrawer.Draw()
                    }
                    else -> {

                    }
                }
            }
        }
    }

    @Composable
    private fun TopStockInfoViewInternal() {
        TopStockInfoView(
            price = vm.price(),
            previousPrice = vm.previousClose(),
            priceChange = vm.priceChange(),
            priceChangeRate = vm.priceChangeRate(),
            volume = vm.volume(),
            open = vm.openPrice(),
            high = vm.highPrice(),
            low = vm.lowPrice(),
        )
    }

    @Composable
    private fun TabViews() {
        val currentTabIndex = currentTab.value.ordinal

        TabRow(
            selectedTabIndex = 0,
            modifier = Modifier
                .padding(horizontal = 2.dp, vertical = 4.dp),
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.background,
            indicator = { tabPositions ->
                val currentTabItem = tabPositions[currentTabIndex]
                TabRowDefaults.Indicator(
                    Modifier.tabIndicatorOffset(currentTabItem)
                )
            },
            divider = {},
        ) {

            OrderTab.entries.forEachIndexed { index, tab ->
                Tab(
                    selected = currentTabIndex == index,
                    onClick = { setOrderTab(tab) },
                    modifier = Modifier.height(32.dp),
                    enabled = index <= 1,
                    icon = {
                        BasicText(
                            s = tab.label,
                            fontSize = 14,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    },
                )
            }
        }
    }
}
