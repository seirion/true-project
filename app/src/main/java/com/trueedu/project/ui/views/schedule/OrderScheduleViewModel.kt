package com.trueedu.project.ui.views.schedule

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.trueedu.project.data.StockPool
import com.trueedu.project.repository.local.Local
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderScheduleViewModel @Inject constructor(
    private val local: Local,
    private val stockPool: StockPool,
): ViewModel() {
    val list = mutableStateOf<List<OrderSchedule>>(emptyList())

    init {
        list.value = local.getOrderSchedule()
    }

    fun add(item: OrderSchedule) {
        val newList = list.value + item
        list.value = newList
        local.setOrderSchedule(newList)
    }

    fun removeAt(index: Int) {
        val newList = list.value.toMutableList().also {
            it.removeAt(index)
        }
        list.value = newList
        local.setOrderSchedule(newList)
    }

    fun nameKr(code: String): String {
        return stockPool.get(code)?.nameKr ?: code
    }
}
