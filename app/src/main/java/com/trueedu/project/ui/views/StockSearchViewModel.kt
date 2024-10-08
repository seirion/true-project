package com.trueedu.project.ui.views

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.model.dto.StockInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class StockSearchViewModel @Inject constructor(
    private val stockPool: StockPool,
): ViewModel() {

    companion object {
        private val TAG = StockSearchViewModel::class.java.simpleName
    }

    val searchInput = mutableStateOf("")
    val searchResult = mutableStateOf<List<StockInfo>>(emptyList())
    val loading = mutableStateOf(true)

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

            launch {
                snapshotFlow { searchInput.value }
                    .debounce(300)
                    .collectLatest {
                        if (it.isEmpty()) {
                            searchResult.value = emptyList()
                        } else {
                            val result = stockPool.search(it)
                            searchResult.value = result
                        }
                    }
            }
        }
    }

    fun updateStocks() {
        stockPool.updateStocks()
    }
}
