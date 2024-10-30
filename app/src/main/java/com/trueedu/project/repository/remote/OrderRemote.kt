package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.model.dto.price.OrderModifiableResponse
import kotlinx.coroutines.flow.Flow

/**
 * 주문 관련 API
 */
interface OrderRemote {
    fun buy(
        accountNum: String,
        code: String,
        price: Int,
        quantity: Int
    ): Flow<OrderResponse>

    fun modifiable(accountNum: String): Flow<OrderModifiableResponse>
}