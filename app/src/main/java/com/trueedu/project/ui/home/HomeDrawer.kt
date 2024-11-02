package com.trueedu.project.ui.home

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
import com.trueedu.project.MainViewModel
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
import com.trueedu.project.ui.views.home.AccountInfo
import com.trueedu.project.ui.views.home.EmptyHome
import com.trueedu.project.ui.views.home.ForceUpdateView
import com.trueedu.project.ui.views.home.StockItem

class HomeDrawer(
    private val vm: MainViewModel,
    private val screen: ScreenControl,
    private val stockPool: StockPool,
    private val admobManager: AdmobManager,
    private val remoteConfig: RemoteConfig,
    private val gotoPlayStore: () -> Unit,
    private val onUserInfo: () -> Unit,
    private val onAccountInfo: () -> Unit,
    private val onWatchList: () -> Unit,
    private val onSearch: () -> Unit,
    private val onMenu: () -> Unit,
    private val onPriceClick: (code: String) -> Unit,
    private val onItemClick: (stockInfo: StockInfo) -> Unit,
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
                        onAccountInfo,
                        onWatchList,
                        onSearch,
                        onMenu,
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
                    contentPadding = PaddingValues(vertical = 16.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    vm.userStocks.value?.output2?.firstOrNull()?.let {
                        item { AccountInfo(it, vm.marketPriceMode.value, vm::onChangeMarketPriceMode) }
                    } ?: item { EmptyHome() }

                    vm.userStocks.value?.output1?.let {
                        val items = it.filter { it.holdingQuantity.toDouble() > 0 }
                        // 광고
                        if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                            item { NativeAdView(admobManager.nativeAd.value!!) }
                        }
                        itemsIndexed(items, { _, item -> item.code} ) { _, item ->
                            StockItem(item, vm.marketPriceMode.value, onPriceClick) {
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
}
