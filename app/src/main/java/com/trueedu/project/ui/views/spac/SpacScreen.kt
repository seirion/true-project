package com.trueedu.project.ui.views.spac

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import com.trueedu.project.MainViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.BottomSelectionFragment
import com.trueedu.project.ui.common.CustomTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.spac.SpacFilterBottomSheet
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
                SpacScreenTopBar(vm.sort.value, ::onSortOption, ::onSpacFilter)
            },
            bottomBar = {
                if (remoteConfig.adVisible.value && admobManager.nativeAd.value != null) {
                    NativeAdView(admobManager.nativeAd.value!!)
                }
            },
            contentWindowInsets =
                ScaffoldDefaults.contentWindowInsets.exclude(NavigationBarDefaults.windowInsets),
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
                    .fillMaxSize()
                    .padding(innerPadding)
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
                    val holdingNum = userStock?.holdingQuantity.safeDouble().takeIf { it > 0 }
                        ?: -vm.holdingNum(item.code)

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
        trueAnalytics.clickButton("${screenName()}__sort_option__click")

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

    // 스팩 필터 도구
    private fun onSpacFilter() {
        trueAnalytics.clickButton("${screenName()}__filter__click")
        SpacFilterBottomSheet.show(fragmentManager)
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

@Preview(showBackground = true)
@Composable
private fun SpacScreenTopBar(
    sortType: SpacSort = SpacSort.ISSUE_DATE,
    onSortOption: () -> Unit = {},
    onFilterOption: () -> Unit = {},
) {
    CustomTopBar(
        navigationIcon = {},
        titleView = {
            TrueText(
                s = "스팩",
                fontSize = 20,
                color = MaterialTheme.colorScheme.primary
            )
        },
        actionsView = {
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .clip(shape = RoundedCornerShape(24.dp))
                    .clickable { onSortOption() }
                    .padding(16.dp, 10.dp, 4.dp, 10.dp)
            ) {
                TrueText(
                    s = sortType.title,
                    fontSize = 16,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    modifier = Modifier.size(24.dp),
                    imageVector = Icons.Filled.ArrowDropDown,
                    tint = MaterialTheme.colorScheme.primary,
                    contentDescription = "sort-select"
                )
            }
            TouchIcon24(icon = Icons.Outlined.ViewList, onClick = onFilterOption)
        }
    )
}
