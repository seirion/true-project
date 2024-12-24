package com.trueedu.project.ui.views.spac

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.ManualAssets
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.utils.formatter.safeDouble
import com.trueedu.project.utils.formatter.safeLong
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SpacViewModel @Inject constructor(
    private val manualAssets: ManualAssets,
    private val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val spacManager: SpacManager,
): ViewModel() {

    companion object {
        private val TAG = SpacViewModel::class.java.simpleName
    }

    val searchInput = mutableStateOf("")
    val stocks = mutableStateOf<List<StockInfo>>(emptyList())
    val sort = mutableStateOf(SpacSort.ISSUE_DATE)

    private val sortFun = mapOf<SpacSort, (StockInfo) -> Double>(
        SpacSort.ISSUE_DATE to { it.listingDate().safeLong().toDouble() },
        SpacSort.MARKET_CAP to {  it.marketCap().safeLong().toDouble() },
        SpacSort.GROWTH_RATE to { -1 * growthRate(it) },
        SpacSort.REDEMPTION_VALUE to { -1 * (spacManager.redemptionValueMap[it.code]?.second ?: Double.MIN_VALUE) },
        SpacSort.VOLUME to { -1 * (spacManager.volumeMap[it.code]?.toDouble() ?: 0.0) },
    )

    init {
        viewModelScope.launch {
            launch {
                spacManager.loading
                    .collect {
                        if (!it) {
                            // 초기값
                            stocks.value = spacManager.spacList.value
                                .filterNot { stockPool.delisted(it.code) }
                                .sortedBy(sortFun[sort.value]!!)
                        }
                    }
            }
            launch {
                snapshotFlow { searchInput.value }
                    .debounce(200)
                    .collectLatest {
                        filterStocks()
                    }
            }
        }
    }

    fun hasAppKey(): Boolean {
        return tokenKeyManager.userKey.value != null
    }

    fun holdingNum(code: String): Double {
        return manualAssets.assets.value
            .firstOrNull { it.code == code }?.quantity ?: 0.0
    }

    fun setSort(option: SpacSort) {
        sort.value = option
        filterStocks()
    }

    private fun filterStocks() {
        stocks.value = spacManager.spacList.value
            .filterNot { stockPool.delisted(it.code) }
            .filter {
                val searchKey = searchInput.value.trim().lowercase()
                searchKey.isEmpty() ||
                        it.nameKr.lowercase().contains(searchKey) ||
                        it.code.lowercase().contains(searchKey)
            }
            .sortedBy(sortFun[sort.value]!!)
    }

    private fun growthRate(stock: StockInfo): Double {
        val code = stock.code
        val prevPrice = stock.prevPrice().safeDouble()
        val price = spacManager.priceMap.getOrDefault(code, prevPrice)
        val base = if (stock.parValue().safeLong() == 100L) 2_000 else 10_000
        return (price - base) * 100.0 / base
    }

    private fun updateOrder() {
        when (sort.value) {
            SpacSort.GROWTH_RATE,
            SpacSort.REDEMPTION_VALUE -> {
                spacManager.spacList.value = stockPool.search(StockInfo::spac)
                    .filterNot { stockPool.delisted(it.code) }
                    .sortedBy(sortFun[sort.value]!!)
            }
            else -> {} // nothing to do
        }
    }
}
