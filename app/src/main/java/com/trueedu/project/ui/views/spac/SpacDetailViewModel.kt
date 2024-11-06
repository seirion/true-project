package com.trueedu.project.ui.views.spac

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.StockInfo
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.repository.remote.PriceRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpacDetailViewModel @Inject constructor(
    private val stockPool: StockPool,
    private val tokenKeyManager: TokenKeyManager,
    private val priceRemote: PriceRemote,
): ViewModel() {
    companion object {
        private val TAG = SpacDetailViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val stock = mutableStateOf<StockInfo?>(null)
    val priceResponse = mutableStateOf<PriceResponse?>(null)

    fun init(code: String) {
        stock.value = stockPool.get(code)
    }

    fun onStart() {
        if (tokenKeyManager.userKey.value == null) return

        viewModelScope.launch {
            priceRemote.currentPrice(stock.value!!.code)
                .collect {
                    if (it.rtCd == "0") priceResponse.value = it
                }
        }
    }

    fun onStop() {
    }
}
