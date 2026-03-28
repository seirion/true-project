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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.activity.ComponentActivity
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.trueedu.project.MainViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.RemoteConfig
import com.trueedu.project.model.dto.firebase.shouldShowRedemption
import com.trueedu.project.ui.ads.AdmobManager
import com.trueedu.project.ui.ads.NativeAdView
import com.trueedu.project.ui.common.BottomSelectionFragment
import com.trueedu.project.ui.common.CustomTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TouchIcon24
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.spac.SpacFilterBottomSheet
import com.trueedu.project.ui.views.StockDetailFragment
import com.trueedu.project.ui.views.order.OrderFragment
import com.trueedu.project.ui.views.search.SearchBar
import com.trueedu.project.ui.views.setting.AppKeyInputFragment
import com.trueedu.project.utils.formatter.safeDouble

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SpacScreen(
    trueAnalytics: TrueAnalytics,
    remoteConfig: RemoteConfig,
    admobManager: AdmobManager,
    fragmentManager: FragmentManager,
    mainVm: MainViewModel = hiltViewModel(LocalContext.current as ComponentActivity),
    vm: SpacViewModel = hiltViewModel(),
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> vm.onStart()
                Lifecycle.Event.ON_STOP -> vm.onStop()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    fun onSortOption() {
        trueAnalytics.clickButton("spac__sort_option__click")
        val selected = SpacSort.entries.indexOfFirst { it == vm.sort.value }
        BottomSelectionFragment.show(
            selected = selected,
            title = "정렬 방법",
            list = SpacSort.entries.map { it.title },
            onSelected = {
                val option = SpacSort.entries[it]
                trueAnalytics.clickButton(
                    "spac__sort__click",
                    mapOf("sort_type" to option.title)
                )
                vm.setSort(option)
            },
            fragmentManager = fragmentManager,
        )
    }

    fun onSpacFilter() {
        trueAnalytics.clickButton("spac__filter__click")
        SpacFilterBottomSheet.show(vm.spacFilter, fragmentManager) {
            if (vm.spacFilter != it) {
                vm.spacFilter = it
                vm.filterStocks()
            }
        }
    }

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
        val loading by vm.spacManager.loading.collectAsState()
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
            stickyHeader { SpacSectionView(vm::setSort) }

            itemsIndexed(vm.stocks.value, key = { i, _ -> i }) { i, item ->

                val spacRefund = vm.spacManager.spacRefundMap.value[item.code]
                val redemptionValue = vm.spacManager.redemptionValueMap[item.code]
                val expectedProfit: Double?
                val expectedProfitRate: Double?
                if (spacRefund?.shouldShowRedemption() == true) {
                    expectedProfit = redemptionValue?.first
                    expectedProfitRate = redemptionValue?.second
                } else {
                    expectedProfit = null
                    expectedProfitRate = null
                }
                val userStock = mainVm.userStocks.value?.output1?.firstOrNull {
                    it.code == item.code
                }

                val holdingNum = userStock?.holdingQuantity.safeDouble().takeIf { it > 0 }
                    ?: -vm.holdingNum(item.code)

                val hasDisclosure = vm.hasDisclosure(item.code)

                SpacItem(i, item,
                    vm.spacManager.priceMap[item.code] ?: 0.0,
                    vm.spacManager.priceChangeMap[item.code],
                    vm.spacManager.volumeMap[item.code] ?: 0L,
                    expectedProfit,
                    expectedProfitRate,
                    holdingNum,
                    hasDisclosure,
                    onPriceClick = { code ->
                        trueAnalytics.clickButton("spac__price__click")
                        if (vm.hasAppKey()) {
                            OrderFragment.show(code, fragmentManager)
                        } else {
                            AppKeyInputFragment.show(false, fragmentManager)
                        }
                    }
                ) {
                    StockDetailFragment.show(item, fragmentManager)
                }
            }
        }
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
