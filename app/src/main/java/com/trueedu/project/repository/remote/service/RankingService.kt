package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.VolumeRankingResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface RankingService {
    @GET("uapi/domestic-stock/v1/quotations/volume-rank")
    suspend fun volumeRanking(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<VolumeRankingResponse>
}