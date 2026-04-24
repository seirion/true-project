package com.trueedu.project.model.ws

/**
 * 실시간 업종지수 (H0UPCNT0)
 * fields:
 * 업종코드|업종현재지수|전일대비부호|전일대비|전일대비율|누적거래량|시가|고가|저가
 */
class RealTimeIndex(
    val data: List<String>
) {
    companion object {
        fun from(rawData: String): RealTimeIndex {
            return RealTimeIndex(
                data = rawData.split("^")
            )
        }
    }

    // 업종코드 (0001: 코스피, 1001: 코스닥)
    val code = data[0]
    // 업종 현재지수
    val price = data[1].toDouble()
    // 전일 대비 부호: 1:상한 2:상승 3:보합 4:하한 5:하락
    val sign = data[2]
    // 전일 대비
    val delta = data[3].toDouble()
    // 전일 대비율(%)
    val rate = data[4].toDouble()
    // 누적 거래량
    val volume = data[5].toDouble()
    // 시가
    val open = data[6].toDouble()
    // 고가
    val high = data[7].toDouble()
    // 저가
    val low = data[8].toDouble()

    // 전일 종가
    val previousClose = price - delta
}
