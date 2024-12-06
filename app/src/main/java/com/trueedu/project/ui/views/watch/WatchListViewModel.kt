package com.trueedu.project.ui.views.watch

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.data.GoogleAccount
import com.trueedu.project.data.realtime.RealPriceManager
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.WatchList
import com.trueedu.project.model.dto.firebase.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.utils.formatter.safeDouble
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
        if (!googleAccount.loggedIn()) {
            loading.value = false
        }

        job = viewModelScope.launch {
            launch {
                combine(
                    snapshotFlow { watchList.list.value },
                    snapshotFlow { currentPage.value }
                ) { list, page ->
                    if (list.isEmpty() || page == null) {
                        emptyList()
                    } else {
                        list[page]
                    }
                }
                    .distinctUntilChanged()
                    .collect { list ->
                        if (list.isEmpty()) return@collect
                        Log.d(TAG, "watchList: $list")
                        loading.value = false

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

    /**
     * 관심 종목을 다른 그룹으로 이동하기
     */
    fun moveTo(index: Int, to: Int) {
        trueAnalytics.clickButton("watch_list__move__click")
        if (currentPage.value != null) {
            val code = watchList.get(currentPage.value!!).getOrNull(index) ?: return
            watchList.removeAt(currentPage.value!!, index)
            watchList.add(to, code)
        }
    }

    fun removeStock(index: Int) {
        trueAnalytics.clickButton("watch_list__remove__click")
        if (currentPage.value != null) {
            watchList.removeAt(currentPage.value!!, index)
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

    fun prevPrice(code: String): Double {
        return getStock(code)?.prevPrice().safeDouble() ?: 0.0
    }
}
