package com.trueedu.project.model.ws

/**
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
