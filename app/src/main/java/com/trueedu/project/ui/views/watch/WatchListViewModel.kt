package com.trueedu.project.ui.views.watch

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val watchList: WatchList,
    private val tokenKeyManager: TokenKeyManager,
    val googleAccount: GoogleAccount,
    val stockPool: StockPool,
    val priceManager: RealPriceManager,
    private val priceRemote: PriceRemote,
    private val trueAnalytics: TrueAnalytics,
): ViewModel() {

    companion object {
        private val TAG = WatchListViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val currentPage = mutableStateOf<Int?>(null)

    /**
     * api 를 통해 받은 가격 기본값
     * 실시간 가격은 웹소켓으로 받아서 사용하지만 그 값을 받기 전까지 사용함
      */
    val basePrices = mutableStateMapOf<String, PriceResponse?>()

    var job: Job? = null

    fun init() {
        job = viewModelScope.launch {
            launch {
                snapshotFlow { watchList.list.value }
                    .filter { it.isNotEmpty() }
                    .collect {
                        Log.d(TAG, "watchList: $it")
                        loading.value = false

                        if (hasAppKey()) {
                            requestRealtimePrice()
                            requestBasePrices()
                        }
                    }
            }

            launch {
                // 페이지가 바뀌면 종목을 변경해 주어야 함
                snapshotFlow { currentPage.value }
                    .distinctUntilChanged()
                    .collect {
                        if (hasAppKey()) {
                            requestRealtimePrice()
                            requestBasePrices()
                        }
                    }
            }
        }
    }

    fun onStop() {
        cancelRealtimePrice()
        job?.cancel()
        job = null
    }

    fun loggedIn() = googleAccount.loggedIn()

    // 일단 고정
    fun pageCount() = WatchList.MAX_GROUP_SIZE

    fun getItems(index: Int): List<String> {
        return watchList.get(index)
    }

    fun getStock(code: String): StockInfo? {
        return stockPool.get(code)
    }

    fun removeStock(code: String) {
        trueAnalytics.clickButton("watch_list__remove__click", mapOf("code" to code))
        if (currentPage.value != null) {
            watchList.remove(currentPage.value!!, code)
        }
    }

    // 현재 페이지의 관심 종목에 대해서 실시간 가격 요청하기
    private fun requestRealtimePrice() {
        if (loading.value || currentPage.value == null) return

        val codes = watchList.get(currentPage.value!!)
        priceManager.pushRequest(TAG, codes)
    }

    private fun cancelRealtimePrice() {
        priceManager.popRequest(TAG)
    }

    private fun requestBasePrices() {
        if (loading.value || currentPage.value == null) return

        watchList.get(currentPage.value!!)
            .filterNot { basePrices.containsKey(it) }
            .forEach { code ->
                priceRemote.currentPrice(code)
                    .onStart {
                        basePrices[code] = null
                    }
                    .catch {
                        Log.d(TAG, "failed to get currentPrice: $code $it")
                        basePrices.remove(code)
                    }
                    .onEach {
                        //Log.d(TAG, "currentPrice: ${it.output.nameKr} ${it.output}")
                        basePrices[code] = it
                    }
                    .launchIn(viewModelScope)
            }
    }

    fun hasAppKey(): Boolean {
        return tokenKeyManager.userKey.value != null
    }
}
