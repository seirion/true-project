package com.trueedu.project.ui.views.stock

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.model.dto.price.DailyPriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import com.trueedu.project.utils.yyyyMMdd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DailyPriceViewModel @Inject constructor(
    private val priceRemote: PriceRemote,
): ViewModel() {
    companion object {
        private val TAG = DailyPriceViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val dailyPrices = mutableStateOf<DailyPriceResponse?>(null)

    // for paging
    private var from = LocalDate.now()
    private var to = LocalDate.now()


    fun init(code: String) {
        // 최대 100건까지 호출 가능
        from = to.minusMonths(3)
        loadData(code)
    }

    fun loadMore(code: String) {
        to = from.minusDays(1)
        from = to.minusMonths(3)
        loadData(code)
    }

    private fun loadData(code: String) {
        priceRemote.dailyPrice(code, from.yyyyMMdd(), to.yyyyMMdd())
            .onEach {
                if (it.rtCd != "0") {
                    val msg = it.msg1
                    Log.d(TAG, "failed to get daily price: $msg")
                    return@onEach
                }
                Log.d(TAG, "dailyPrice: $it")
                if (dailyPrices.value == null) {
                    val prices = it.dailyPrices.filter {
                        it.date != null
                    }
                    dailyPrices.value = it.copy(
                        dailyPrices = prices
                    )
                } else {
                    val org = dailyPrices.value
                    val list = org!!.dailyPrices.toMutableList()
                    list.addAll(it.dailyPrices.filter { it.date != null })
                    dailyPrices.value = org.copy(
                        dailyPrices = list
                    )
                }
                loading.value = false
            }
            .launchIn(viewModelScope)
    }
}