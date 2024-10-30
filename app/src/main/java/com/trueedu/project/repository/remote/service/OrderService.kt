package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.model.dto.price.OrderModifiableResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST
import retrofit2.http.QueryMap

interface OrderService {
    @POST("uapi/domestic-stock/v1/trading/order-cash")
    suspend fun buy(
        @HeaderMap headers: Map<String, String>,
        @Body body: Map<String, String>
    ): Response<OrderResponse>

    @GET("uapi/domestic-stock/v1/trading/inquire-psbl-rvsecncl")
    suspend fun modifiable(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<OrderModifiableResponse>
}
