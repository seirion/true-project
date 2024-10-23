package com.trueedu.project.repository.remote

import com.trueedu.project.model.dto.price.PriceResponse
import com.trueedu.project.model.dto.price.TradeResponse
import kotlinx.coroutines.flow.Flow

/**
 * 각종 시세 관련 API
 */
interface PriceRemote {
    fun currentPrice(code: String): Flow<PriceResponse>

    // 주식 호가, 체결
    fun currentTrade(code: String): Flow<TradeResponse>
}
