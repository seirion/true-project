package com.trueedu.project.ui.views.order

import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.price.OrderModifiableResponse
import com.trueedu.project.repository.remote.OrderRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
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
    val checked = mutableStateMapOf<String, Boolean>()

    fun init() {
        update()
    }

    fun update() {
        val accountNum = tokenKeyManager.userKey.value?.accountNum ?: return
        orderRemote.modifiable(accountNum)
            .onStart {
                loading.value = true
                checked.clear()
            }
            .onEach {
                Log.d(TAG, "정정/취소 목록: $it")
                loading.value = false
                items.value = it
            }
            .catch {
                Log.d(TAG, "정정/취소 실패: $it")
            }
            .launchIn(viewModelScope)
    }

    fun onChecked(code: String) {
        if (checked.containsKey(code)) {
            checked.remove(code)
        } else {
            checked[code] = true
        }
    }
}
