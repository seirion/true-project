package com.trueedu.project.ui.views

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.BasicText
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest

@AndroidEntryPoint
class WatchListFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): WatchListFragment {
            val fragment = WatchListFragment()
            fragment.show(fragmentManager, "watch-list")
            return fragment
        }
    }

    private val vm by viewModels<WatchListViewModel>()

    @OptIn(ExperimentalFoundationApi::class)
    private var pagerState: PagerState? = null

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BodyScreen() {
        val currentPage = remember { mutableStateOf("") }
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState?.currentPage }
                .collectLatest { page ->
                    currentPage.value = page?.let {
                        (it % vm.pageCount()).toString()
                    } ?: ""
                }
        }
        Scaffold(
            topBar = { BackTitleTopBar("관심 종목 ${currentPage.value}", ::dismissAllowingStateLoss) },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            if (pagerState == null) {
                pagerState = rememberPagerState(
                    initialPage = 10000 * vm.pageCount(),
                    initialPageOffsetFraction = 0f,
                    pageCount = { 20000 * vm.pageCount() }, // infinite loop
                )
            }

            HorizontalPager(
                state = pagerState!!,
                modifier = Modifier.fillMaxSize()
            ) { position ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        BasicText(
                            s = "${position % vm.pageCount()}",
                            fontSize = 28,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }

    private fun gotoStockDetail(stockInfo: StockInfo) {
        StockDetailFragment.show(stockInfo, parentFragmentManager)
    }
}
