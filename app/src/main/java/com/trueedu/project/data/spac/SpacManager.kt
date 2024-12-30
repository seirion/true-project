package com.trueedu.project.data.spac

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.firebase.SpacStatusManager
import com.trueedu.project.model.dto.firebase.SpacStatus
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.utils.formatter.safeDouble
import com.trueedu.project.utils.formatter.safeLong
import com.trueedu.project.utils.redemptionProfitRate
import com.trueedu.project.utils.stringToLocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpacManager @Inject constructor(
    private val local: Local,
    private val tokenKeyManager: TokenKeyManager,
    private val stockPool: StockPool,
    private val spacStatusManager: SpacStatusManager,
    private val priceRemote: PriceRemote,
) {

    companion object {
        private val TAG = SpacManager::class.java.simpleName
    }

    val spacStatusMap = mutableStateOf<Map<String, SpacStatus>>(emptyMap())

    val loading = MutableStateFlow(true)
    val spacList = mutableStateOf<List<StockInfo>>(emptyList())
    val priceMap = mutableStateMapOf<String, Double>()
    val priceChangeMap = mutableStateMapOf<String, Double>()
    val volumeMap = mutableStateMapOf<String, Long>()
    val redemptionValueMap = mutableStateMapOf<String, Pair<Int, Double>>()

    val spacAnnualProfitMode = mutableStateOf(local.spacAnnualProfit)

    private var job: Job? = null
    private var requestIndex = 0

    init {
        MainScope().launch {
            combine(
                stockPool.status.filter { it == StockPool.Status.SUCCESS },
                flow { emit(spacStatusManager.load()) }
            ) { _, spacStatuses -> spacStatuses }
                .collect {
                    Log.d(TAG, "spac status init: ${it.size}")
                    spacList.value = stockPool.search(StockInfo::spac)
                    spacStatusMap.value = it.associateBy(SpacStatus::code)
                    init()
                }
        }
    }

    private fun init() {
        // 초기 값으로 전일 종가를 줌
        spacList.value.forEach {
            priceMap[it.code] = it.prevPrice().safeDouble()
            volumeMap[it.code] = it.prevVolume().safeLong()
            updateRedemptionValue(it.code)
        }

        loading.value = false
    }

    fun onStart() {
        job = MainScope().launch {
            loading.collect {
                if (!it) beginLoading()
            }
        }
    }

    private suspend fun beginLoading() {
        if (tokenKeyManager.userKey.value == null) return

        flow {
            while (true) {
                delay(100)
                emit(requestIndex++)
            }
        }.collect { i ->
            val size = spacList.value.size
            if (size == 0) return@collect
            val s = spacList.value.getOrNull(i % size) ?: return@collect
            try {
                priceRemote.currentPrice(s.code)
                    .filterNot { it.output == null }
                    .collect {
                        priceMap[s.code] = it.output!!.price.toDouble()
                        priceChangeMap[s.code] = it.output.priceChange.toDouble()
                        volumeMap[s.code] = it.output.volume.safeLong()

                        // 가격 변경 시 청산 시 수익률도 업데이트 필요
                        updateRedemptionValue(s.code)
                    }
            } catch(it: Exception) {
                Log.e(TAG, "price error: $it")
            }
        }
    }

    fun onStop() {
        job?.cancel()
        job = null
    }

    fun setSpacAnnualProfit(on: Boolean) {
        local.spacAnnualProfit = on
        spacAnnualProfitMode.value = on

        // 재계산
        loading.value = true
        spacList.value.forEach {
            updateRedemptionValue(it.code)
        }
        loading.value = false
    }

    private fun updateRedemptionValue(code: String) {
        val stock = stockPool.get(code) ?: return
        val price = priceMap[code] ?: stock.prevPrice().safeDouble()
        val redemptionPrice = spacStatusMap.value[code]?.redemptionPrice ?: return
        val listingDateStr = stockPool.get(code)?.listingDate() ?: return
        val targetDate = stringToLocalDate(listingDateStr)
            .plusYears(3)
        val isAnnualized = spacAnnualProfitMode.value
        val (valueRate, valueRateAnnualized) = redemptionProfitRate(price, redemptionPrice, targetDate)
        val rate = if (isAnnualized) valueRateAnnualized else valueRate

        if (rate != null) {
            redemptionValueMap[code] = redemptionPrice to rate
        }
    }
}
