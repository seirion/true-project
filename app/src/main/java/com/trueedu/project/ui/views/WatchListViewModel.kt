package com.trueedu.project.ui.views

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WatchListViewModel @Inject constructor(
): ViewModel() {

    companion object {
        private val TAG = WatchListViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)

    init {

        viewModelScope.launch {
            launch {
            }
        }
    }

    // 일단 고정
    fun pageCount() = 10
}
