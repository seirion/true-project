package com.trueedu.project.ui.views.order

import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.trueedu.project.R
import com.trueedu.project.data.UserAssets
import com.trueedu.project.model.dto.price.OrderModifiableDetail
import com.trueedu.project.repository.local.Local
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.TrueText
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
    private val executionVm by viewModels<OrderExecutionViewModel>()

    @Inject
    lateinit var local: Local
    @Inject
    lateinit var userAssets: UserAssets

    private lateinit var orderViewDrawer: OrderViewDrawer
    private lateinit var modifiableViewDrawer: ModifiableViewDrawer
    private lateinit var orderExecutionDrawer: OrderExecutionDrawer
    private lateinit var balanceDrawer: BalanceDrawer

    private val currentTab = mutableStateOf(OrderTab.Order)

    private fun setOrderTab(tab: OrderTab) {
        currentTab.value = tab
        local.setOrderTab(tab)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenSheetKeyboardDialogTheme)
    }

    override fun init() {
        super.init()
        orderViewDrawer = OrderViewDrawer(vm, modifyVm, ::buy, ::sell, ::modifyOrder, vm::setQuantity)
        modifiableViewDrawer = ModifiableViewDrawer(modifyVm, ::cancelOrder, ::gotoOrder)
        orderExecutionDrawer = OrderExecutionDrawer(executionVm, ::gotoOrder)
        balanceDrawer = BalanceDrawer(userAssets, ::gotoOrder)

        vm.init(code, null)
        modifyVm.init()

        lifecycleScope.launch {
            snapshotFlow { currentTab.value }
                .collect {
                    if (it == OrderTab.Modification) {
                        modifyVm.init()
                    } else if (it == OrderTab.Execution) {
                        executionVm.init()
                    } else if (it == OrderTab.Balance) {
                        balanceDrawer.init()
                    }
                }
        }
    }

    private fun gotoOrder(code: String, originalOrder: OrderModifiableDetail? = null) {
        if (vm.stockPool.get(code) == null) {
            Toast.makeText(requireContext(), "상장 폐지 종목입니다", Toast.LENGTH_SHORT).show()
            return
        }
        setOrderTab(OrderTab.Order)
        vm.init(code, originalOrder)
    }

    override fun onDestroy() {
        super.onDestroy()
        vm.destroy()
    }

    private fun buy() {
        trueAnalytics.clickButton("${screenName()}__buy__click")
        vm.buy(
            onSuccess = {
                Toast.makeText(requireContext(), "매수 주문이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                modifyVm.update()
            },
            onFail = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun sell() {
        trueAnalytics.clickButton("${screenName()}__sell__click")
        vm.sell(
            onSuccess = {
                Toast.makeText(requireContext(), "매도 주문이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                modifyVm.update()
            },
            onFail = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun modifyOrder(order: OrderModifiableDetail) {
        trueAnalytics.clickButton("${screenName()}__modify__click")
        modifyVm.modify(
            orderNo = order.orderNo,
            priceString = vm.priceInput.value.text,
            quantityString = vm.quantityInput.value.text,
            onSuccess = {
                Toast.makeText(requireContext(), "주문이 수정되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onFail = {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            },
        )
    }

    private fun cancelOrder(orderNo: String) {
        trueAnalytics.clickButton("${screenName()}__cancel__click")
        modifyVm.cancel(
            orderNo = orderNo,
            onSuccess = {
                Toast.makeText(requireContext(), "주문이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            },
            onFail = { msg ->
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        )
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                val stockName = vm.nameKr.value
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
            ) {
                TopStockInfoViewInternal()
                TabViews()

                if (currentTab.value != OrderTab.Order) {
                    // 정정 주문 데이터 삭제
                    vm.originalOrder.value = null
                }

                when (currentTab.value) {
                    OrderTab.Order -> {
                        orderViewDrawer.Draw()
                    }
                    OrderTab.Modification -> {
                        modifiableViewDrawer.Draw()
                    }
                    OrderTab.Execution -> {
                        orderExecutionDrawer.Draw()
                    }
                    OrderTab.Balance -> {
                        balanceDrawer.Draw()
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
                    enabled = true,
                    icon = {
                        TrueText(
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
