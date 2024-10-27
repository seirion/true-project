package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.order.OrderResponse
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
}
