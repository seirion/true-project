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
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpacViewModel @Inject constructor(
    val manualAssets: ManualAssets,
    val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val priceRemote: PriceRemote,
): ViewModel() {

    companion object {
        private val TAG = SpacViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(false)
    val totalValues = mutableStateOf(SpacProfit.empty)

    /**
     * api 를 통해 받은 현재 가격
     */
    val priceMap = mutableStateMapOf<String, Double>()

    private var job: Job? = null
    private var requestIndex = 0

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
                            }

                            StockPool.Status.UPDATING -> {
                            }

                            StockPool.Status.FAIL -> {
                            }
                        }
                    }
            }
        }
    }

    fun onStart() {
        if (tokenKeyManager.userKey.value == null) return

        job = viewModelScope.launch {
            flow {
                while (true) {
                    delay(100)
                    emit(requestIndex++)
                }
            }.collect {
                val size = manualAssets.assets.value.size
                if (size == 0) return@collect
                val s = manualAssets.assets.value.getOrNull(it % size) ?: return@collect
                priceRemote.currentPrice(s.code)
                    .collect {
                        try {
                            priceMap[s.code] = it.output.price.toDouble()
                            updateTotalValues()
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

    private fun updateTotalValues() {
        if (loading.value) return

        val totalCost = manualAssets.assets.value.sumOf { it.price * it.quantity }
        val totalValue = manualAssets.assets.value.sumOf {
            val p = priceMap[it.code] ?: stockPool.get(it.code)?.prevPrice()?.toDouble() ?: it.price
            p * it.quantity
        }
        totalValues.value = SpacProfit(totalCost, totalValue)
    }
}
