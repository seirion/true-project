package com.trueedu.project.ui.views.spac

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.ManualAssets
import com.trueedu.project.data.StockPool
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SpacViewModel @Inject constructor(
    val manualAssets: ManualAssets,
    val stockPool: StockPool,
): ViewModel() {
    val loading = mutableStateOf(false)

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
