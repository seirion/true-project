package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.model.dto.price.TradeResponse
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.PriceService
import kotlinx.coroutines.flow.Flow

class PriceRemoteImpl(
    @NormalService
    private val priceService: PriceService
): PriceRemote {
    override fun currentPrice(code: String) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "FHKST01010100",
            "custtype" to "P",
        )
        val queries = mapOf(
            "FID_COND_MRKT_DIV_CODE" to "J", // 주식, ETF, ETN
            "FID_INPUT_ISCD" to code,
        )
        priceService.currentPrice(headers, queries)
    }

    override fun currentTrade(code: String) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "FHKST01010200",
            "custtype" to "P",
        )
        val queries = mapOf(
            "FID_COND_MRKT_DIV_CODE" to "J", // 주식, ETF, ETN
            "FID_INPUT_ISCD" to code,
        )
        priceService.currentTrade(headers, queries)
    }

    override fun dailyPrice(
        code: String,
        from: String, // yyyyMMdd
        to: String, // yyyyMMdd
    ) = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "FHKST03010100",
            "custtype" to "P",
        )
        val queries = mapOf(
            "FID_COND_MRKT_DIV_CODE" to "J", // 주식, ETF, ETN
            "FID_INPUT_ISCD" to code,
            "FID_INPUT_DATE_1" to from,
            "FID_INPUT_DATE_2" to to,
            "FID_PERIOD_DIV_CODE" to "D", // D:일봉, W:주봉, M:월봉, Y:년봉
            "FID_ORG_ADJ_PRC" to "1", // 	0:수정주가 1:원주가
        )
        priceService.dailyPrice(headers, queries)
    }
}
