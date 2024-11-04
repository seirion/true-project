package com.trueedu.project.ui.home

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.MainViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.base.ComposableDrawer
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.ScreenControl
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.theme.TrueProjectTheme
import com.trueedu.project.ui.topbar.MainTopBar
import com.trueedu.project.ui.views.MenuFragment
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.home.AccountInfo
import com.trueedu.project.ui.views.home.EmptyHome
import com.trueedu.project.ui.views.home.ForceUpdateView
import com.trueedu.project.ui.views.home.StockItem
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.StockSearchFragment
import com.trueedu.project.ui.views.setting.AppKeyInputFragment

class HomeDrawer(
    private val activity: Activity,
    private val vm: MainViewModel,
    private val screen: ScreenControl,
    private val stockPool: StockPool,
    private val admobManager: AdmobManager,
    private val remoteConfig: RemoteConfig,
    private val trueAnalytics: TrueAnalytics,
    private val fragmentManager: FragmentManager,
    private val gotoPlayStore: () -> Unit,
    private val onUserInfo: () -> Unit,
    private val onWatchList: () -> Unit,
): ComposableDrawer {
    @Composable
    override fun Draw() {
        TrueProjectTheme(
            n = screen.theme.intValue,
            forceDark = screen.forceDark.value
        ) {
            if (vm.forceUpdateVisible.value) {
                ForceUpdateView(gotoPlayStore)
                return@TrueProjectTheme
            }
            Scaffold(
                topBar = {
                    MainTopBar(
                        vm.googleSignInAccount.value,
                        vm.accountNum.value,
                        onUserInfo,
                        ::onAccountInfo,
                        onWatchList,
                        ::onSearch,
                        ::onMenu,
                    )
                },
                modifier = Modifier.fillMaxSize(),
            ) { innerPadding ->

                if (vm.loading.value) {
                    LoadingView()
                    return@Scaffold
                }

                val state = rememberLazyListState()
                LazyColumn(
                    state = state,
                    contentPadding = PaddingValues(top = 8.dp, bottom = 56.dp),
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
                    } ?: item { EmptyHome() }

                    vm.userStocks.value?.output1?.let {
                        val items = it.filter { it.holdingQuantity.toDouble() > 0 }
                        // 광고
                        if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                            item { NativeAdView(admobManager.nativeAd.value!!) }
                        }
                        itemsIndexed(items, { _, item -> item.code} ) { _, item ->
                            StockItem(item, vm.marketPriceMode.value, ::onPriceClick) {
                                stockPool.get(item.code)?.let {
                                    onItemClick(it)
                                }
                            }
                        }
                    }
                }
            }
        }
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

    private fun onMenu() {
        trueAnalytics.clickButton("${screenName()}__menu__click")
        MenuFragment.show(fragmentManager)
    }

    private fun onPriceClick(code: String) {
        trueAnalytics.clickButton("${screenName()}__price__click")
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
