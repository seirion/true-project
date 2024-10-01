package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.rank.VolumeRankingResponse
import kotlinx.coroutines.flow.Flow

interface RankingRemote {
    fun getVolumeRanking(): Flow<VolumeRankingResponse>
}
