package com.trueedu.project.ui.views.home

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.trueedu.project.MainViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.StockPool
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.Margin
import com.trueedu.project.ui.topbar.MainTopBar
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.StockSearchFragment
import com.trueedu.project.ui.views.setting.AppKeyInputFragment

@Composable
fun HomeScreen(
    stockPool: StockPool,
    admobManager: AdmobManager,
    remoteConfig: RemoteConfig,
    trueAnalytics: TrueAnalytics,
    fragmentManager: FragmentManager,
    onUserInfo: () -> Unit,
    vm: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    trueAnalytics.log("home__enter")
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            MainTopBar(
                googleAccount = vm.googleSignInAccount.value,
                accountNum = vm.accountNum.value,
                onUserInfoClick = onUserInfo,
                onAccountInfoClick = {
                    trueAnalytics.clickButton("home__account_info__click")
                    AppKeyInputFragment.show(false, fragmentManager)
                },
                onSearchClick = {
                    trueAnalytics.clickButton("home__stock_search__click")
                    StockSearchFragment.show(null, fragmentManager)
                },
            )
        },
        contentWindowInsets =
            ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        // 초기 로딩 (데이터 없음): 스켈레톤 표시
        if (vm.loading.value && vm.userStocks.value == null) {
            HomeLoadingSkeleton(modifier = Modifier.padding(innerPadding))
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
            // 새로고침 중 (기존 데이터 있음): 상단 작은 인디케이터
            if (vm.loading.value) {
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            vm.userStocks.value?.output2?.firstOrNull()?.let {
                item {
                    AccountInfo(
                        it,
                        vm.marketPriceMode.value,
                        onRefresh = {
                            trueAnalytics.clickButton("home__refresh__click")
                            vm.refresh {
                                Toast.makeText(
                                    context,
                                    "자산 정보를 갱신했습니다.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
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
                    HomeStockItem(
                        item = item,
                        stock = stock,
                        marketPriceMode = vm.marketPriceMode.value,
                        onPriceClick = { code ->
                            trueAnalytics.clickButton("home__price__click")
                            if (stockPool.get(code) == null) {
                                Toast.makeText(context, "상장 폐지 종목입니다", Toast.LENGTH_SHORT).show()
                                return@HomeStockItem
                            }
                            OrderFragment.show(code, fragmentManager)
                        },
                        onItemClick = { code ->
                            stockPool.get(code)?.let { stockInfo ->
                                trueAnalytics.clickButton("home__item__click")
                                StockDetailFragment.show(stockInfo, fragmentManager)
                            }
                        },
                    )
                }
            }
        }
    }
}
