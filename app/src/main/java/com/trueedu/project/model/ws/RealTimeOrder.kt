package com.trueedu.project.model.ws

/**
 * 실시간 호가
 * fields:
 */
class RealTimeOrder(
    val data: List<String>
) {
    companion object {
        fun from(rawData: String): RealTimeOrder {
            return RealTimeOrder(
                data = rawData.split("^")
            )
        }
    }

    val code = data[0]

    // TODO: 테스트 필요함
    // print("영업시간 [" + recvvalue[1] + "]" + "시간구분코드 [" + recvvalue[2] + "]")

    // 총 매도호가 잔량
    val totalSellQuantity = data[43]
    // 총 매수호가 잔량
    val totalBuyQuantity = data[44]

    // 누적거래량
    val volume = data[53]

    /**
     * 매도 10 호가: <가격, 잔량>
     */
    fun sells(): List<Pair<Double, Double>> {
        val prices = 3..12
        val counts = 23..32
        return prices.zip(counts).map { (p, c) ->
            data[p].toDouble() to data[c].toDouble()
        }.reversed() // 매도 10호가는 내림차순
    }

    /**
     * 매수 10 호가: <가격, 잔량>
     */
    fun buys(): List<Pair<Double, Double>> {
        val prices = 13..22
        val counts = 33..42
        return prices.zip(counts).map { (p, c) ->
            data[p].toDouble() to data[c].toDouble()
        }
    }
}
