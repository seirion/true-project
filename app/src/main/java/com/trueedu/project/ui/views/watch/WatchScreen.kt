package com.trueedu.project.ui.views.watch

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentManager
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.DividerHorizontal
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.graphics.DrawCandle
import com.trueedu.project.ui.theme.ChartColor
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.common.DesignatedBadge
import com.trueedu.project.ui.views.common.DisclosurePoint
import com.trueedu.project.ui.views.common.HaltBadge
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.StockSearchFragment
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.utils.formatter.CashFormatter
import com.trueedu.project.utils.formatter.RateFormatter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.mapNotNull

class WatchScreen(
    private val activity: Activity,
    private val vm: WatchListViewModel,
    private val admobManager: AdmobManager,
    private val remoteConfig: RemoteConfig,
    private val trueAnalytics: TrueAnalytics,
    private val fragmentManager: FragmentManager,
): BottomNavScreen {
    companion object {
        private val TAG = WatchScreen::class.java.simpleName
    }

    private var pagerState: PagerState? = null

    @Composable
    override fun Draw() {
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState?.currentPage }
                .mapNotNull { it?.mod(vm.pageCount()) }
                .collectLatest {
                    vm.currentPage.value = it
                }
        }
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = vm.groupName(vm.currentPage.value),
                    onBack = null,
                    actionIcon = Icons.Filled.Search,
                    onAction = ::onSearch,
                    actionIcon2 = Icons.Filled.Edit,
                    onAction2 = ::onEdit,
                )
            },
            bottomBar = {
                if (
                    !vm.loading.value &&
                    vm.currentPage.value != null &&
                    vm.getItems(vm.currentPage.value!!).isNotEmpty() &&
                    remoteConfig.adVisible.value &&
                    admobManager.nativeAd.value != null
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        NativeAdView(admobManager.nativeAd.value!!)
                    }
                }
            },
            contentWindowInsets =
                ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->

            if (pagerState == null) {
                pagerState = rememberPagerState(
                    initialPage = 100 * vm.pageCount(),
                    initialPageOffsetFraction = 0f,
                    pageCount = { 200 * vm.pageCount() }, // infinite loop
                )
            }

            val status by vm.stockPool.status.collectAsState()
            // 주식 정보와 관심 종목 정보를 모두 받아야 데이터 표시 가능
            if (vm.loading.value || status != StockPool.Status.SUCCESS) {
                LoadingView()
                return@Scaffold
            }

            HorizontalPager(
                state = pagerState!!,
                modifier = Modifier.fillMaxSize()
            ) { position ->
                var selectedStock by remember { mutableStateOf<StockInfo?>(null) }
                var selectedStockIndex by remember { mutableIntStateOf(-1) }
                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    val items = vm.getItems(position % vm.pageCount())
                    itemsIndexed(items, key = { _, item -> item }) { index, code ->
                        val stock = vm.getStock(code) ?: return@itemsIndexed
                        val tradeData = vm.priceManager.dataMap[code]
                        val basePrice = vm.basePrices[code]?.output

                        // appkey 가 없으면 그냥 전일 가격을 표시함
                        val price = tradeData?.price ?: basePrice?.price?.toDouble() ?: vm.prevPrice(code)
                        val delta = tradeData?.delta ?: basePrice?.priceChange?.toDouble()
                        val rate = tradeData?.rate ?: basePrice?.priceChangeRate?.toDouble()
                        val volume = tradeData?.volume ?: basePrice?.volume?.toDouble() ?: 0.0

                        WatchingStockItem(
                            nameKr = stock.nameKr,
                            code = code,
                            price = price,
                            prevClose = tradeData?.previousClose ?: basePrice?.previousClosePrice?.toDouble(),
                            open = tradeData?.open ?: basePrice?.open?.toDouble(),
                            high = tradeData?.high ?: basePrice?.high?.toDouble(),
                            low = tradeData?.low ?: basePrice?.low?.toDouble(),
                            delta = delta,
                            rate = rate,
                            volume = volume,
                            halt = stock.halt(),
                            designated = stock.designated(),
                            hasDisclosure = vm.hasDisclosure(code),
                            onTradingClick = { gotoTrading(stock) },
                            onClick = { gotoStockDetail(stock) },
                        ) {
                            Log.d(TAG, "long click: ${stock.nameKr}")
                            selectedStock = stock
                            selectedStockIndex = index
                        }
                    }
                } // end of LazyColumn

                if (selectedStock != null) {
                    Dialog(
                        onDismissRequest = { selectedStock = null },
                        properties = DialogProperties(usePlatformDefaultWidth = false) // Important for custom positioning
                    ) {
                        PopupBody(selectedStock!!, position, selectedStockIndex,
                            moveTo = { index, toPage ->
                                selectedStock = null
                                vm.moveTo(index, toPage)
                            },
                            onRemove = {
                                selectedStock = null
                                vm.removeStock(selectedStockIndex)
                            },
                        )
                    }
                }
            }
        }
    }

    override fun onStart() {
        vm.init()
    }

    override fun onStop() {
        vm.onStop()
    }

    private fun doAfterLogin(action: () -> Unit) {
        if (vm.googleAccount.loggedIn()) {
            action()
        } else {
            vm.googleAccount.login(activity, action)
        }
    }

    private fun onSearch() {
        trueAnalytics.clickButton("watch_list__search__click")
        doAfterLogin {
            StockSearchFragment.show(vm.currentPage.value, fragmentManager)
        }
    }

    private fun onEdit() {
        trueAnalytics.clickButton("watch_list__edit__click")
        doAfterLogin {
            if (!vm.loading.value || vm.currentPage.value != null) {
                WatchEditFragment.show(vm.currentPage.value!!, fragmentManager)
            }
        }
    }

    private fun gotoTrading(stockInfo: StockInfo) {
        if (vm.hasAppKey()) {
            OrderFragment.show(stockInfo.code, fragmentManager)
        } else {
            AppKeyInputFragment.show(false, fragmentManager)
        }
    }

    private fun gotoStockDetail(stockInfo: StockInfo) {
        StockDetailFragment.show(stockInfo, fragmentManager)
    }

    @Composable
    fun PopupBody(
        item: StockInfo,
        page: Int,
        index: Int,
        moveTo: (Int, Int) -> Unit,
        onRemove: () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(
                    MaterialTheme.colorScheme.background,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            TrueText(
                s = "${item.nameKr} 이동",
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
            )
            Margin(8)
            DividerHorizontal()
            Column(modifier = Modifier.fillMaxWidth()) {
                repeat(vm.pageCount()) {
                    TrueText(
                        s = vm.groupName(it),
                        fontSize = 16,
                        color = MaterialTheme.colorScheme.primary
                            .copy(alpha = if (it == page) 0.1f else 1f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (it != page) {
                                    moveTo(index, it)
                                }
                            }
                            .padding(vertical = 8.dp),
                    )
                    DividerHorizontal()
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onRemove() }
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TrueText(
                    s = "관심종목에서 삭제",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16,
                    style = TextStyle(textDecoration = TextDecoration.Underline),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            Margin(8)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WatchingStockItem(
    nameKr: String,
    code: String,
    price: Double,
    prevClose: Double?,
    open: Double?,
    high: Double?,
    low: Double?,
    delta: Double?,
    rate: Double?,
    volume: Double,
    halt: Boolean,
    designated: Boolean,
    hasDisclosure: Boolean,
    onTradingClick: () -> Unit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val formatter = CashFormatter()
    val rateFormatter = RateFormatter()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                if (hasDisclosure) {
                    DisclosurePoint()
                }
                TrueText(
                    s = nameKr,
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                if (halt) {
                    Margin(2)
                    HaltBadge()
                }
                if (designated) {
                    Margin(2)
                    DesignatedBadge()
                }
            }
            TrueText(
                s = "(${code})",
                fontSize = 13,
                color = MaterialTheme.colorScheme.secondary,
            )
        }

        Row(
            modifier = Modifier.width(128.dp)
                .height(40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            // ohlc 데이터가 있으면 캔들 표시
            if (prevClose != null && open != null && high != null && low != null) {
                DrawCandle(
                    prevClose = prevClose,
                    open = open,
                    high = high,
                    low = low,
                    close = price,
                )
            } else {
                // 정렬을 위해
                Margin(1)
            }

            val priceColor = ChartColor.color(delta ?: 0.0)
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier
                    .clickable { onTradingClick() }
            ) {
                val totalValueString = formatter.format(price)
                TrueText(
                    s = totalValueString,
                    fontSize = 14,
                    fontWeight = FontWeight.W600,
                    color = priceColor,
                )

                val deltaString = if (delta != null && rate != null) {
                    val profitString = formatter.format(delta, true)
                    val profitRateString = rateFormatter.format(rate, true)
                    "$profitString ($profitRateString)"
                } else {
                    "-"
                }
                TrueText(s = deltaString, fontSize = 12, color = priceColor)
            }
        }
    }
}
