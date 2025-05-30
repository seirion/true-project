package com.trueedu.project.ui.views.home

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.MainViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.topbar.MainTopBar
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.StockSearchFragment
import com.trueedu.project.ui.views.setting.AppKeyInputFragment

class HomeScreen(
    private val activity: Activity,
    private val vm: MainViewModel,
    private val stockPool: StockPool,
    private val admobManager: AdmobManager,
    private val remoteConfig: RemoteConfig,
    private val trueAnalytics: TrueAnalytics,
    private val fragmentManager: FragmentManager,
    private val onUserInfo: () -> Unit,
): BottomNavScreen {
    companion object {
        private val TAG = HomeScreen::class.java.simpleName
    }

    @Composable
    override fun Draw() {
        Scaffold(
            topBar = {
                MainTopBar(
                    vm.googleSignInAccount.value,
                    vm.accountNum.value,
                    onUserInfo,
                    ::onAccountInfo,
                    ::onSearch,
                )
            },
            contentWindowInsets =
                ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->

            if (vm.loading.value) {
                LoadingView()
                return@Scaffold
            }

            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                contentPadding = PaddingValues(top = 8.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                vm.userStocks.value?.output2?.firstOrNull()?.let {
                    item {
                        AccountInfo(
                            it,
                            vm.marketPriceMode.value,
                            ::onRefresh,
                            vm::onChangeMarketPriceMode
                        )
                    }
                } ?: item {
                    Margin(32)
                    EmptyHome()
                }

                vm.userStocks.value?.output1?.let {
                    val items = it.filter { it.holdingQuantity.toDouble() > 0 }
                    // 광고
                    if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                        item { NativeAdView(admobManager.nativeAd.value!!) }
                    }
                    itemsIndexed(items, { _, item -> item.code} ) { _, item ->
                        val stock = stockPool.get(item.code)
                        HomeStockItem(item, stock, vm.marketPriceMode.value, ::onPriceClick) {
                            stockPool.get(item.code)?.let {
                                onItemClick(it)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        trueAnalytics.log("${screenName()}__enter")
        Log.d(TAG, "onStart")
    }

    override fun onStop() {
        Log.d(TAG, "onStop")
    }

    private fun onItemClick(stockInfo: StockInfo) {
        trueAnalytics.clickButton("${screenName()}__item__click")
        StockDetailFragment.show(stockInfo, fragmentManager)
    }

    private fun onAccountInfo() {
        trueAnalytics.clickButton("${screenName()}__account_info__click")
        AppKeyInputFragment.show(false, fragmentManager)
    }

    private fun onSearch() {
        trueAnalytics.clickButton("${screenName()}__stock_search__click")
        StockSearchFragment.show(null, fragmentManager)
    }

    private fun onPriceClick(code: String) {
        trueAnalytics.clickButton("${screenName()}__price__click")
        if (stockPool.get(code) == null) {
            // 상장 폐지 종목이라서 주문으로 이동 안 함
            Toast.makeText(activity.applicationContext, "상장 폐지 종목입니다", Toast.LENGTH_SHORT).show()
            return
        }
        OrderFragment.show(code, fragmentManager)
    }

    private fun onRefresh() {
        trueAnalytics.clickButton("${screenName()}__refresh__click")
        vm.refresh {
            Toast.makeText(
                activity.applicationContext,
                "자산 정보를 갱신했습니다.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}
