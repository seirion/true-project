package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.order.OrderModifyResponse
import com.trueedu.project.model.dto.order.OrderResponse
import com.trueedu.project.model.dto.order.ScheduleOrderCancelResponse
import com.trueedu.project.model.dto.order.ScheduleOrderResponse
import com.trueedu.project.model.dto.order.ScheduleOrderResult
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

    fun modify(
        accountNum: String,
        originalOrderCode: String,
        priceString: String,
        quantityString: String,
    ): Flow<OrderModifyResponse>

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

    /**
     * 주식 매수/매도 예약 - 목록
     */
    fun scheduleOrderList(
        accountNum: String,
    ): Flow<ScheduleOrderResult>

    /**
     * 주식 매수/매도 예약
     * 예약주문 가능시간 : 15시 40분 ~ 다음 영업일 7시 30분
     *  (단, 서버 초기화 작업 시 예약주문 불가 : 23시 40분 ~ 00시 10분)
     */
    fun scheduleOrder(
        accountNum: String,
        code: String,
        isBuy: Boolean,
        price: String,
        quantity: String,
        endDate: String,
    ): Flow<ScheduleOrderResponse>

    /**
     * 주식 매수/매도 예약 취소
     */
    fun cancelScheduleOrder(
        accountNum: String,
        orderSeq: String,
    ): Flow<ScheduleOrderCancelResponse>
}
