package com.trueedu.project.repository.remote

import com.trueedu.project.di.NormalService
import com.trueedu.project.network.apiCallFlow
import com.trueedu.project.repository.remote.service.RankingService

class RankingRemoteImpl(
    @NormalService
    private val rankingService: RankingService
): RankingRemote {

    override fun getVolumeRanking() = apiCallFlow {
        val headers = mapOf(
            "tr_id" to "FHPST01710000",
            "custtype" to "P",
        )
        val queries = mapOf(
            "FID_COND_MRKT_DIV_CODE" to "J",
            "FID_COND_SCR_DIV_CODE" to "20171",
            "FID_INPUT_ISCD" to "0000", // 0000(전체) 기타(업종코드)
            "FID_DIV_CLS_CODE" to "0", // 0(전체) 1(보통주) 2(우선주)
            "FID_BLNG_CLS_CODE" to "0", // 0 : 평균거래량 1:거래증가율 2:평균거래회전율 3:거래금액순 4:평균거래금액회전율
            "FID_TRGT_CLS_CODE" to "111111111", // 1 or 0 9자리 (차례대로 증거금 30% 40% 50% 60% 100% 신용보증금 30% 40% 50% 60%) ex) "111111111"
            "FID_TRGT_EXLS_CLS_CODE" to "0000000000", // 1 or 0 10자리 (차례대로 투자위험/경고/주의 관리종목 정리매매 불성실공시 우선주 거래정지 ETF ETN 신용주문불가 SPAC) ex) "0000000000"
            // 전체 가격 대상 조회 시 FID_INPUT_PRICE_1, FID_INPUT_PRICE_2 모두 ""(공란) 입력
            "FID_INPUT_PRICE_1" to "",
            "FID_INPUT_PRICE_2" to "",
            // 전체 거래량 대상 조회 시 FID_VOL_CNT ""(공란) 입력
            "FID_VOL_CNT" to "",
            "FID_INPUT_DATE_1" to "", // 공란
        )
        rankingService.volumeRanking(headers, queries)
    }
}
