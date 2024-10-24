package com.trueedu.project.model.ws

/**
 * fields:
 * 유가증권단축종목코드|주식체결시간|주식현재가|전일대비부호|전일대비|전일대비율|가중평균주식가격|주식시가|주식최고가|주식최저가|매도호가1|매수호가1|체결거래량|누적거래량|누적거래대금|매도체결건수|매수체결건수|순매수체결건수|체결강도|총매도수량|총매수수량|체결구분|매수비율|전일거래량대비등락율|시가시간|시가대비구분|시가대비|최고가시간|고가대비구분|고가대비|최저가시간|저가대비구분|저가대비|영업일자|신장운영구분코드|거래정지여부|매도호가잔량|매수호가잔량|총매도호가잔량|총매수호가잔량|거래량회전율|전일동시간누적거래량|전일동시간누적거래량비율|시간구분코드|임의종료구분코드|정적VI발동기준가
 */
class RealTimeTrade(
    val data: List<String>
) {
    companion object {
        fun from(rawData: String): RealTimeTrade {
            return RealTimeTrade(
                data = rawData.split("^")
            )
        }
    }
    // 단축코드
    val code = data[0]
    // 주식체결시간
    val datetime = data[1] // 'HHmmss'
    // 주식현재가
    val price = data[2].toDouble()
    // 전일대비부호: 1 : 상한 2 : 상승 3 : 보합 4 : 하한 5 : 하락
    // val sign = data[3]
    // 전일대비 증감
    val delta = data[4].toDouble()
    // 전일대비율(%)
    val rate = data[5].toDouble()
    // 누적거래량
    val volume = data[14].toDouble()

    // 전일 종가
    val previousClose = price - delta
}
