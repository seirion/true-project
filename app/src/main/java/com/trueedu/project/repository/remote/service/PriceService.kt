package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.price.DailyPriceResponse
import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.model.dto.price.TradeResponse
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

    @GET("uapi/domestic-stock/v1/quotations/inquire-asking-price-exp-ccn")
    suspend fun currentTrade(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<TradeResponse>

    @GET("uapi/domestic-stock/v1/quotations/inquire-daily-itemchartprice")
    suspend fun dailyPrice(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<DailyPriceResponse>
}
