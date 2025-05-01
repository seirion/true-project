package com.trueedu.project.ui.views.schedule

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.order.ScheduleOrderResult
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.OrderRemote
import com.trueedu.project.utils.isHoliday
import com.trueedu.project.utils.yyyyMMdd
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OrderScheduleViewModel @Inject constructor(
    private val local: Local,
    private val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val orderRemote: OrderRemote,
): ViewModel() {

    companion object {
        private val TAG = OrderScheduleViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val list = mutableStateOf<ScheduleOrderResult?>(null)

    init {
        load()
    }

    private fun load(fk200: String = "", nk200: String = "") {
        viewModelScope.launch {
            val userKey = tokenKeyManager.userKey.value ?: return@launch
            orderRemote.scheduleOrderList(userKey.accountNum ?: "", fk200, nk200)
                .collect {
                    Log.d(TAG, "예약 데이터: $it")
                    loading.value = false
                    list.value = it
                }
        }
    }

    fun add(item: OrderSchedule, onFailed: (String) -> Unit) {
        val userKey = tokenKeyManager.userKey.value
        if (userKey == null) {
            Log.d(TAG, "add(): no user key")
            return
        }

        val endDate = LocalDate.now()
            .plusDays(30)
            .let {
                var date = it
                while (date.isHoliday()) {
                    date = date.minusDays(1)
                }
                date
            }
            .yyyyMMdd()

        orderRemote.scheduleOrder(
            accountNum = userKey.accountNum ?: "",
            code = item.code,
            isBuy = item.isBuy,
            price = item.price.toString(),
            quantity = item.quantity.toString(),
            endDate = endDate,
        ).onEach {
            if (it.rtCd == "0") {
                load()
            } else {
                onFailed(it.msg ?: "예약 주문 실패")
            }
        }.launchIn(viewModelScope)
    }

    fun removeAt(index: Int, onFailed: (String) -> Unit) {
        val userKey = tokenKeyManager.userKey.value
        if (userKey == null) {
            Log.d(TAG, "removeAt(): no user key")
            return
        }
        orderRemote.cancelScheduleOrder(
            userKey.accountNum ?: "",
            list.value?.list?.get(index)?.seq?: ""
        ).onEach {
            if (it.rtCd == "0") {
                load()
            } else {
                onFailed(it.msg ?: it.msg1 ?: "예약 취소 실패")
            }
        }.launchIn(viewModelScope)
    }

    fun nameKr(code: String): String {
        return stockPool.get(code)?.nameKr ?: code
    }
}
