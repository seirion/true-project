package com.trueedu.project.ui.views.order

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.price.OrderExecutionResponse
import com.trueedu.project.repository.remote.OrderRemote
import com.trueedu.project.utils.yyyyMMdd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OrderExecutionViewModel @Inject constructor(
    private val tokenKeyManager: TokenKeyManager,
    private val orderRemote: OrderRemote,
): ViewModel() {

    companion object {
        private val TAG = OrderExecutionViewModel::class.java.simpleName
    }
    val loading = mutableStateOf(false)
    val response = mutableStateOf<OrderExecutionResponse?>(null)

    fun init() {
        update()
    }

    fun update() {
        val dateString = LocalDate.now().yyyyMMdd()
        Log.d(TAG, "체결 정보 요청: $dateString")
        val accountNum = tokenKeyManager.userKey.value?.accountNum ?: return
        orderRemote.orderExecution(
            accountNum,
            "",
            dateString,
            dateString
        )
            .onStart {
                loading.value = true
            }
            .onEach {
                Log.d(TAG, "체결 정보: $it")
                loading.value = false
                response.value = it
            }
            .catch {
                Log.d(TAG, "체결 정보 받기 실패: $it")
            }
            .launchIn(viewModelScope)
    }
}
