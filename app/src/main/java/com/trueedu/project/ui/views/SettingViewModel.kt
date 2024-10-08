package com.trueedu.project.ui.views

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.StockPool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val stockPool: StockPool,
): ViewModel() {

    companion object {
        private val TAG = SettingViewModel::class.java.simpleName
    }

    val updateAvailable = mutableStateOf(false)
    val stockUpdateLabel = mutableStateOf("로딩중")

    init {
        viewModelScope.launch {
            snapshotFlow { stockPool.status.value }
                .collect { status ->
                    if (status == StockPool.Status.SUCCESS) {
                        updateAvailable.value = stockPool.needUpdate()
                        stockUpdateLabel.value = if (updateAvailable.value) {
                            ""
                        } else {
                            " - 최신 데이터"
                        }
                    } else {
                        updateAvailable.value = false
                        stockUpdateLabel.value = " - 로딩 실패"
                    }
                }
        }
    }
}
