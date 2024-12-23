package com.trueedu.project.ui.views.spac

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import com.trueedu.project.MainViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BottomSelectionFragment
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.home.BottomNavScreen
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.utils.formatter.safeDouble

class SpacScreen(
    private val mainVm: MainViewModel,
    private val vm: SpacViewModel,
    private val spacManager: SpacManager,
    private val trueAnalytics: TrueAnalytics,
    private val remoteConfig: RemoteConfig,
    private val admobManager: AdmobManager,
    private val fragmentManager: FragmentManager,
): BottomNavScreen {
    companion object {
        private val TAG = SpacScreen::class.java.simpleName
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun Draw() {
        Scaffold(
            topBar = {
                BackTitleTopBar(
                    title = "스팩 검색",
                    onBack = null,
                    actionIcon = Icons.Outlined.ViewList,
                    onAction = ::onSortOption,
                )
            },
            bottomBar = {
                if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                    NativeAdView(admobManager.nativeAd.value!!)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->
            val loading by spacManager.loading.collectAsState()
            if (loading) {
                LoadingView()
                return@Scaffold
            }

            val state = rememberLazyListState()

            LaunchedEffect(key1 = loading) {
                state.scrollToItem(1)
            }

            LazyColumn(
                state = state,
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                item { SearchBar(searchText = vm.searchInput) {} }
                stickyHeader { SpacSectionView() }

                itemsIndexed(vm.stocks.value, key = { i, _ -> i }) { i, item ->
                    val redemptionValue = spacManager.redemptionValueMap[item.code]
                    val expectedProfit = redemptionValue?.first
                    val expectedProfitRate = redemptionValue?.second
                    val userStock = mainVm.userStocks.value?.output1?.firstOrNull {
                        it.code == item.code
                    }

                    // 한투 계좌 보유가 있으면 표시하고, 없으면 수동 보유를 표시함
                    val holdingNum = userStock?.holdingQuantity.safeDouble()
                        .coerceAtLeast(vm.holdingNum(item.code))

                    SpacItem(i, item,
                        spacManager.priceMap[item.code] ?: 0.0,
                        spacManager.priceChangeMap[item.code],
                        spacManager.volumeMap[item.code] ?: 0L,
                        expectedProfit,
                        expectedProfitRate,
                        holdingNum,
                        ::onPriceClick
                    ) {
                        StockDetailFragment.show(item, fragmentManager)
                    }
                }
            }
        }
    }

    // 정렬하기
    private fun onSortOption() {
        val selected = SpacSort.entries.indexOfFirst { it == vm.sort.value }
        BottomSelectionFragment.show(
            selected = selected,
            title = "정렬 방법",
            list = SpacSort.entries.map { it.title },
            onSelected = {
                val option = SpacSort.entries[it]
                trueAnalytics.clickButton(
                    "${screenName()}__sort__click",
                    mapOf("sort_type" to option.title)
                )
                vm.setSort(option)
            },
            fragmentManager = fragmentManager,
        )
    }

    private fun onPriceClick(code: String) {
        trueAnalytics.clickButton("${screenName()}__price__click")
        if (vm.hasAppKey()) {
            OrderFragment.show(code, fragmentManager)
        } else {
            AppKeyInputFragment.show(false, fragmentManager)
        }
    }

    override fun onStart() {
        super.onStart()
        spacManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        spacManager.onStop()
    }
}
