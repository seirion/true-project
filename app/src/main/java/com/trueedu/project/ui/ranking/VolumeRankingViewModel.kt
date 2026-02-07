package com.trueedu.project.ui.ranking

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.log.logD
import com.trueedu.project.data.log.logE
import com.trueedu.project.model.dto.rank.VolumeRankingOutput
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.RankingRemote
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class VolumeRankingViewModel @Inject constructor(
    private val local: Local,
    rankingRemote: RankingRemote,
): ViewModel() {
    val loading = mutableStateOf(true)
    val loadingFail = mutableStateOf(false)
    val list = mutableListOf<VolumeRankingOutput>()

    init {
        rankingRemote.getVolumeRanking()
            .catch {
                logE("failed to get VolumeRanking: $it")
                loadingFail.value = true
            }
            .onEach {
                if (it.rtCd == "0") {
                    logD("new volumeRanking: ${it.output.size}")
                    list.addAll(it.output)
                } else {
                    loadingFail.value = true
                }
            }
            .onCompletion {
                loading.value = false
            }
            .launchIn(viewModelScope)
    }
}
