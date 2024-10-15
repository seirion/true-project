package com.trueedu.project.ui.views

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.WatchList
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
    private val watchList: WatchList,
): ViewModel() {

    companion object {
        private val TAG = WatchListViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)

    init {
        viewModelScope.launch {
            snapshotFlow { watchList.list.value }
                .filter { it.isNotEmpty() }
                .collect {
                    Log.d(TAG, "watchList: $it")
                    loading.value = false
                }
            }
    }

    // 일단 고정
    fun pageCount() = WatchList.MAX_GROUP_SIZE

    fun getItems(index: Int): List<String> {
        return watchList.get(index)
    }
}
