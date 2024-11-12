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
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpacListViewModel @Inject constructor(
    private val manualAssets: ManualAssets,
    private val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val priceRemote: PriceRemote,
): ViewModel() {
    companion object {
        private val TAG = SpacListViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val stocks = mutableStateOf<List<StockInfo>>(emptyList())
    val priceMap = mutableStateMapOf<String, Double>()

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
                                    .sortedBy(StockInfo::listingDate) // 상장순으로
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
}