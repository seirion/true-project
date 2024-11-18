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
import com.trueedu.project.utils.formatter.numberFormat
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
    val spacStatus = mutableStateOf<Map<String, SpacStatus>>(emptyMap())

    val sort = mutableStateOf(SpacSort.ISSUE_DATE)

    private val sortFun = mapOf(
        SpacSort.ISSUE_DATE to { it: StockInfo -> it.listingDate().safeLong() },
        SpacSort.MARKET_CAP to { it: StockInfo -> it.marketCap().safeLong() },
        SpacSort.GROWTH_RATE to { it: StockInfo -> it.prevPrice().safeLong() }, // FIXME
        SpacSort.VOLUME to { it: StockInfo -> it.prevVolume().safeLong() },
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
                            }

                            StockPool.Status.UPDATING -> {
                            }

                            StockPool.Status.FAIL -> {
                            }
                        }
                    }
            }
            launch {
                spacStatus.value = spacStatusManager.load()
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
