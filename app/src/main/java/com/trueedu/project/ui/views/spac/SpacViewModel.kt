package com.trueedu.project.ui.views.spac

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpacViewModel @Inject constructor(
    private val stockPool: StockPool,
): ViewModel() {
    companion object {
        private val TAG = SpacViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val stocks = mutableStateOf<List<StockInfo>>(emptyList())

    init {
        viewModelScope.launch {
            launch {
                snapshotFlow { stockPool.status.value }
                    .collect { status ->
                        when (status) {
                            StockPool.Status.LOADING -> {
                            }

                            StockPool.Status.SUCCESS -> {
                                loading.value = false
                                stocks.value = stockPool.search(StockInfo::spac)
                                    .sortedBy(StockInfo::listingDate) // 상장순으로
                            }

                            StockPool.Status.UPDATING -> {
                            }

                            StockPool.Status.FAIL -> {
                            }
                        }
                    }
            }
        }
    }
}