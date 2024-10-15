package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.StockInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val watchList: WatchList,
    private val stockPool: StockPool,
    val priceManager: RealPriceManager,
): ViewModel() {

    companion object {
        private val TAG = WatchListViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val currentPage = mutableStateOf<Int?>(null)

    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { watchList.list.value }
                    .filter { it.isNotEmpty() }
                    .collect {
                        Log.d(TAG, "watchList: $it")
                        loading.value = false

                        Log.d("aaaa", "1")
                        requestRealtimePrice()
                    }
            }

            launch {
                snapshotFlow { priceManager.dataMap.values }
                    .collect {
                        Log.d(TAG, "price updated")
                    }
            }
        }
    }

    // 일단 고정
    fun pageCount() = WatchList.MAX_GROUP_SIZE

    fun getItems(index: Int): List<String> {
        return watchList.get(index)
    }

    fun getStock(code: String): StockInfo? {
        return stockPool.get(code)
    }

    // 현재 페이지의 관심 종목에 대해서 실시간 가격 요청하기
    fun requestRealtimePrice() {
        if (loading.value || currentPage.value == null) return

        val codes = watchList.get(currentPage.value!!)
        priceManager.pushRequest(TAG, codes)
    }
}
