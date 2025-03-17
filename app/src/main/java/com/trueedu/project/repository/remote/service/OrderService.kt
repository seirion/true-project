package com.trueedu.project.repository.remote.service

import com.trueedu.project.model.dto.order.OrderModifyResponse
import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.model.dto.order.ScheduleOrderCancelResponse
import com.trueedu.project.model.dto.order.ScheduleOrderResponse
import com.trueedu.project.model.dto.order.ScheduleOrderResult
import com.trueedu.project.model.dto.price.OrderExecutionResponse
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

    @POST("uapi/domestic-stock/v1/trading/order-rvsecncl")
    suspend fun modify(
        @HeaderMap headers: Map<String, String>,
        @Body body: Map<String, String>
    ): Response<OrderModifyResponse>

    @GET("uapi/domestic-stock/v1/trading/inquire-daily-ccld")
    suspend fun orderExecution(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<OrderExecutionResponse>

    @GET("/uapi/domestic-stock/v1/trading/order-resv-ccnl")
    suspend fun scheduleOrderResult(
        @HeaderMap headers: Map<String, String>,
        @QueryMap queries: Map<String, String>
    ): Response<ScheduleOrderResult>

    @POST("/uapi/domestic-stock/v1/trading/order-resv")
    suspend fun scheduleOrder(
        @HeaderMap headers: Map<String, String>,
        @Body body: Map<String, String>
    ): Response<ScheduleOrderResponse>

    @POST("/uapi/domestic-stock/v1/trading/order-resv-rvsecncl")
    suspend fun cancelScheduleOrder(
        @HeaderMap headers: Map<String, String>,
        @Body body: Map<String, String>
    ): Response<ScheduleOrderCancelResponse>
}
