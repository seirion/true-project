package com.trueedu.project.ui.views.ipo

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.StockPool
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.model.dto.rank.IpoScheduleResponse
import com.trueedu.project.repository.remote.RankingRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IpoScheduleViewModel @Inject constructor(
    private val rankingRemote: RankingRemote,
): ViewModel() {
    companion object {
        private val TAG = IpoScheduleViewModel::class.java.simpleName
    }

    val loading = mutableStateOf(true)
    val ipoSchedule = mutableStateOf<IpoScheduleResponse?>(null)

    init {
        val from = "20241010"
        val to = "20241230"
        rankingRemote.ipoSchedule(from, to)
            .onEach {
                Log.d(TAG, "공모주 일정 데이터: $it")
                ipoSchedule.value = it
            }
            .catch {
                Log.e(TAG, "공모주 일정 데이터 받기 실패: $it")
            }
            .onCompletion {
                loading.value = false
            }
            .launchIn(viewModelScope)
    }
}