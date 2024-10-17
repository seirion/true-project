package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.price.PriceResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.QueryMap

interface PriceService {
    @GET("uapi/domestic-stock/v1/quotations/inquire-price")
    suspend fun currentPrice(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<PriceResponse>
}
