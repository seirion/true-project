package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.order.OrderModifyResponse
import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.model.dto.price.OrderExecutionResponse
import com.trueedu.project.model.dto.price.OrderModifiableResponse
import kotlinx.coroutines.flow.Flow

/**
 * 주문 관련 API
 */
interface OrderRemote {
    fun buy(
        accountNum: String,
        code: String,
        price: String,
        quantity: String,
    ): Flow<OrderResponse>

    fun sell(
        accountNum: String,
        code: String,
        price: String,
        quantity: String,
    ): Flow<OrderResponse>

    fun modifiable(accountNum: String): Flow<OrderModifiableResponse>

    fun cancel(
        accountNum: String,
        originalOrderCode: String,
    ): Flow<OrderModifyResponse>

    /**
     * 주문 체결 내역
     * 일 별로 조회 가능하지만 일단 당일 조회만 하게 구현
     */
    fun orderExecution(
        accountNum: String,
        code: String,
        fromDate: String, // yyyyMMdd
        toDate: String, // yyyyMMdd
    ): Flow<OrderExecutionResponse>
}
