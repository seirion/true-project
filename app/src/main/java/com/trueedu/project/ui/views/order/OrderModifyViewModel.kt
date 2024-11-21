package com.trueedu.project.ui.views.order

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.price.OrderModifiableResponse
import com.trueedu.project.repository.remote.OrderRemote
import com.trueedu.project.ui.views.order.OrderViewModel.Companion
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

@HiltViewModel
class OrderModifyViewModel @Inject constructor(
    private val tokenKeyManager: TokenKeyManager,
    private val orderRemote: OrderRemote,
): ViewModel() {

    companion object {
        private val TAG = OrderModifyViewModel::class.java.simpleName
    }
    val loading = mutableStateOf(false)
    val items = mutableStateOf<OrderModifiableResponse?>(null)

    fun init() {
        update()
    }

    fun update() {
        val accountNum = tokenKeyManager.userKey.value?.accountNum ?: return
        orderRemote.modifiable(accountNum)
            .onStart {
                loading.value = true
            }
            .onEach {
                Log.d(TAG, "정정/취소 목록: $it")
                loading.value = false
                items.value = it
            }
            .catch {
                Log.d(TAG, "정정/취소 목록 받기 실패: $it")
            }
            .launchIn(viewModelScope)
    }

    fun cancel(
        orderNo: String,
        onSuccess: () -> Unit,
        onFail: (String) -> Unit
    ) {
        val accountNum = tokenKeyManager.userKey.value?.accountNum ?: return

        orderRemote.cancel(accountNum, orderNo)
            .flowOn(Dispatchers.IO)
            .onEach {
                if (it.rtCd == "0") {
                    onSuccess()
                } else {
                    Log.d(TAG, "주문 취소 실패: $it")
                    onFail(it.msg ?: it.msg1 ?: "주문 실패")
                }
            }
            .catch {
                Log.d(TAG, "취소 실패: $it")
                onFail("주문 취소 실패")
            }
            .onCompletion {
                update()
            }
            .flowOn(Dispatchers.Main)
            .launchIn(viewModelScope)
    }
}
