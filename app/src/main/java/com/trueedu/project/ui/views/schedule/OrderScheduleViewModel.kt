package com.trueedu.project.ui.views.schedule

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.trueedu.project.analytics.TrueAnalytics
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.OrderRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OrderScheduleViewModel @Inject constructor(
    private val local: Local,
    private val orderRemote: OrderRemote,
    private val trueAnalytics: TrueAnalytics,
): ViewModel() {
    val list = mutableStateOf<List<OrderSchedule>>(emptyList())

    init {
        list.value = local.getOrderSchedule()
    }

    fun add() {

    }

    fun removeAt(index: Int) {

    }
}
