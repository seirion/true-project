package com.trueedu.project.ui.spac

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.ui.BaseFragment
import com.trueedu.project.ui.common.BackTitleTopBar
import com.trueedu.project.ui.common.LoadingView
import com.trueedu.project.ui.common.TrueText
import com.trueedu.project.ui.views.spac.SpacItem
import com.trueedu.project.utils.formatter.intFormatter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SpacAnalysisFragment: BaseFragment() {
    companion object {
        fun show(
            fragmentManager: FragmentManager
        ): SpacAnalysisFragment {
            val fragment = SpacAnalysisFragment()
            fragment.show(fragmentManager, "spac-analysis")
            return fragment
        }
    }

    @Inject
    lateinit var stockPool: StockPool

    @Inject
    lateinit var spacManager: SpacManager

    private val loading = mutableStateOf(false)
    val stocks = mutableStateOf<List<StockInfo>>(emptyList())

    override fun init() {
        super.init()

        lifecycleScope.launch {
            launch {
                spacManager.loading
                    .collect {
                        if (!it) {
                            stocks.value = spacManager.spacList.value
                        }
                    }
            }
            launch {
                snapshotFlow { spacManager.volumePriceMap.size }
                    .collectLatest {
                        if (stocks.value.isNotEmpty() && it == stocks.value.size) {
                            stocks.value = stocks.value
                                .sortedByDescending {
                                    spacManager.volumeMap[it.code] ?: 0L
                                }
                        }
                    }
            }
        }
    }

    @Composable
    override fun BodyScreen() {
        Scaffold(
            topBar = {
                val loadingNum = if (loading.value) {
                    ""
                } else {
                    if (spacManager.volumePriceMap.size >= stocks.value.size) {
                        val a = spacManager.volumePriceMap.keys
                        val b = stocks.value.map { it.code }.toSet()
                    }
                    "${spacManager.volumePriceMap.size}/${stocks.value.size}"
                }

                BackTitleTopBar("스팩 통계 분석 $loadingNum", ::dismissAllowingStateLoss)
            },
            modifier = Modifier
                .fillMaxSize()
                .background(color = MaterialTheme.colorScheme.background),
        ) { innerPadding ->

            if (loading.value) {
                LoadingView()
                return@Scaffold
            }

            val state = rememberLazyListState()
            LazyColumn(
                state = state,
                modifier = Modifier.padding(innerPadding)
            ) {
                item { VolumeSumView() }
                itemsIndexed(stocks.value, key = { i, _ -> i }) { i, item ->
                    SpacItem(
                        i, item,
                        spacManager.priceMap[item.code] ?: 0.0,
                        spacManager.priceChangeMap[item.code],
                        spacManager.volumeMap[item.code] ?: 0L,
                        null,
                        null,
                        0.0,
                        {},
                    ) {}
                }
            }
        }
    }

    @Composable
    private fun VolumeSumView() {
        val volumeSum = spacManager.volumeMap.values.sum().let {
            intFormatter.format(it)
        }
        val volumePriceSum = spacManager.volumePriceMap.values.sum().let {
            intFormatter.format(it)
        }
        Column(modifier = Modifier.padding(8.dp)) {
            TrueText(
                s = "총 거래량: $volumeSum",
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )
            TrueText(
                s = "총 거래 대금: $volumePriceSum",
                fontSize = 14,
                color = MaterialTheme.colorScheme.primary,
            )
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
