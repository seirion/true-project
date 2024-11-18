package com.trueedu.project.ui.views.spac

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.ManualAssets
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.model.dto.firebase.SpacStatus
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.utils.formatter.safeDouble
import com.trueedu.project.utils.formatter.safeLong
import com.trueedu.project.utils.stringToLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class SpacListViewModel @Inject constructor(
    private val manualAssets: ManualAssets,
    private val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val spacStatusManager: SpacStatusManager,
    private val priceRemote: PriceRemote,
): ViewModel() {
    companion object {
        private val TAG = SpacListViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val stocks = mutableStateOf<List<StockInfo>>(emptyList())
    val priceMap = mutableStateMapOf<String, Double>()
    val spacStatusMap = mutableStateOf<Map<String, SpacStatus>>(emptyMap())

    // 청산 가격과 수익률을 미리 구해 둔다
    val redemptionValueMap = mutableStateMapOf<String, Pair<Int, Double>>() // value, rate

    val sort = mutableStateOf(SpacSort.ISSUE_DATE)

    private val sortFun = mapOf(
        SpacSort.ISSUE_DATE to { it: StockInfo -> it.listingDate().safeLong().toDouble() },
        SpacSort.MARKET_CAP to { it: StockInfo -> it.marketCap().safeLong().toDouble() },
        SpacSort.GROWTH_RATE to { it: StockInfo -> growthRate(it.prevPrice().safeLong()) },
        SpacSort.REDEMPTION_VALUE to { redemptionValueMap[it.code]?.second ?: Double.MIN_VALUE },
        SpacSort.VOLUME to { it: StockInfo -> it.prevVolume().safeLong().toDouble() },
    )

    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { stockPool.status.value }
                    .collect { status ->
                        when (status) {
                            StockPool.Status.LOADING -> {
                            }

                            StockPool.Status.SUCCESS -> {
                                loading.value = false
                                stocks.value = stockPool.search(StockInfo::spac)
                                    .sortedBy(sortFun[sort.value]!!)

                                // 초기 값으로 전일 종가를 줌
                                stocks.value.forEach {
                                    priceMap[it.code] = it.prevPrice().safeDouble()
                                }
                            }

                            StockPool.Status.UPDATING -> {
                            }

                            StockPool.Status.FAIL -> {
                            }
                        }
                    }
            }
            launch {
                spacStatusMap.value = spacStatusManager.load()
                    .associateBy { it.code }
            }
        }
    }

    private var job: Job? = null
    private var requestIndex = 0

    fun onStart() {
        if (tokenKeyManager.userKey.value == null) return

        job = viewModelScope.launch {
            flow {
                while (true) {
                    delay(100)
                    emit(requestIndex++)
                }
            }.collect {
                val size = stocks.value.size
                if (size == 0) return@collect
                val s = stocks.value.getOrNull(it % size) ?: return@collect
                priceRemote.currentPrice(s.code)
                    .collect {
                        try {
                            priceMap[s.code] = it.output.price.toDouble()
                            updateRedemptionValue(s.code)
                            updateOrder() // 순서 갱신
                        } catch (e: NumberFormatException) {
                            Log.d(TAG, "prcie format error: ${it.output.price}\n$e")
                        }
                    }
            }
        }
    }

    fun onStop() {
        job?.cancel()
    }

    fun hasAppKey(): Boolean {
        return tokenKeyManager.userKey.value != null
    }

    fun hasStock(code: String): Boolean {
        return manualAssets.assets.value.any { it.code == code }
    }

    fun setSort(option: SpacSort) {
        sort.value = option
        stocks.value = stockPool.search(StockInfo::spac)
            .sortedBy(sortFun[sort.value]!!)
    }

    private fun growthRate(price: Long): Double {
        val base = if (price > 8_000) 10_000 else 2_000
        return (price - base) * 100.0 / base
    }

    private fun updateRedemptionValue(code: String) {
        val price = priceMap[code] ?: return
        val redemptionPrice = spacStatusMap.value[code]?.redemptionPrice ?: return
        val listingDateStr = stockPool.get(code)?.listingDate() ?: return

        val now = LocalDate.now()
        val targetDate = stringToLocalDate(listingDateStr)
            .plusYears(3)
        //.plusMonths(-2)
        val daysBetween = ChronoUnit.DAYS.between(now, targetDate)
        if (daysBetween <= 0) return

        // 1년 환산 수익률로 변환하기
        val valueRate = (redemptionPrice - price) / price * 365 / daysBetween * 100
        redemptionValueMap[code] = redemptionPrice to valueRate
    }

    private fun updateOrder() {
        when (sort.value) {
            SpacSort.GROWTH_RATE,
            SpacSort.REDEMPTION_VALUE -> {
                stocks.value = stockPool.search(StockInfo::spac)
                    .sortedBy(sortFun[sort.value]!!)
            }
            else -> {} // nothing to do
        }
    }

    /**
     * (청산 가격 - 현재 가격)의 1년 환산 수익률
     */
    fun rateOf(listingDateStr: String?, currentPrice: Double?, redemptionPrice: Int): Double? {
        if (listingDateStr == null || currentPrice == null) return null
        val now = LocalDate.now()
        val targetDate = stringToLocalDate(listingDateStr)
            .plusYears(3)
            //.plusMonths(-2)
        val daysBetween = ChronoUnit.DAYS.between(now, targetDate)
        if (daysBetween <= 0) return null

        // 1년 환산 수익률로 변환하기
        return (redemptionPrice - currentPrice) / currentPrice * 365 / daysBetween * 100
    }
}
