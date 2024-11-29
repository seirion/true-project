package com.trueedu.project.ui.views.spac

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.trueedu.project.data.ManualAssets
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.spac.SpacManager
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.repository.local.Local
import com.trueedu.project.utils.formatter.safeLong
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SpacListViewModel @Inject constructor(
    private val local: Local,
    private val manualAssets: ManualAssets,
    private val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val spacManager: SpacManager,
): ViewModel() {
    companion object {
        private val TAG = SpacListViewModel::class.java.simpleName
    }

    val sort = mutableStateOf(SpacSort.ISSUE_DATE)

    private val sortFun = mapOf<SpacSort, (StockInfo) -> Double>(
        SpacSort.ISSUE_DATE to { it.listingDate().safeLong().toDouble() },
        SpacSort.MARKET_CAP to {  it.marketCap().safeLong().toDouble() },
        SpacSort.GROWTH_RATE to { -1 * growthRate(it.prevPrice().safeLong()) },
        SpacSort.REDEMPTION_VALUE to { -1 * (spacManager.redemptionValueMap[it.code]?.second ?: Double.MIN_VALUE) },
        SpacSort.VOLUME to { -1 * (spacManager.volumeMap[it.code]?.toDouble() ?: 0.0) },
    )

    fun hasAppKey(): Boolean {
        return tokenKeyManager.userKey.value != null
    }

    fun hasStock(code: String): Boolean {
        return manualAssets.assets.value.any { it.code == code }
    }

    fun setSort(option: SpacSort) {
        sort.value = option
        spacManager.spacList.value = stockPool.search(StockInfo::spac)
            .filterNot { stockPool.delisted(it.code) }
            .sortedBy(sortFun[sort.value]!!)
    }

    private fun growthRate(price: Long): Double {
        val base = if (price > 8_000) 10_000 else 2_000
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
