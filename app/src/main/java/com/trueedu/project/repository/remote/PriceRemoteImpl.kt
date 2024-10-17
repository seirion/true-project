package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.PriceService

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
}
