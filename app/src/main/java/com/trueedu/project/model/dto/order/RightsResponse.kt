package com.trueedu.project.model.dto.order

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 기간별 계좌 권리 현황 조회 응답
 */
@Serializable
data class RightsResponse(
    @SerialName("rt_cd")
    val rtCd: String, // 성공 실패 여부 "0" 성공
    @SerialName("msg_cd")
    val msgCd: String, // 응답코드
    @SerialName("msg1")
    val msg: String, // 응답메세지
    @SerialName("output")
    val list: List<RightsDetail>?,
    @SerialName("ctx_area_fk100")
    val fk100: String?,
    @SerialName("ctx_area_nk100")
    val nk100: String?,
)

@Serializable
data class RightsDetail(
    @SerialName("bass_dt")
    val baseDate: String? = null, // 기준일자
    @SerialName("rptt_pdno")
    val representativeProductNo: String? = null, // 대표상품번호
    @SerialName("pdno")
    val productNo: String? = null, // 상품번호
    @SerialName("prdt_type_cd")
    val productTypeCode: String? = null, // 상품유형코드
    @SerialName("rght_type_cd")
    val rightsTypeCode: String? = null, // 권리유형코드
    @SerialName("shtn_pdno")
    val code: String? = null, // 단축상품번호
    @SerialName("prdt_name")
    val nameKr: String? = null, // 상품명
    @SerialName("cblc_qty")
    val balanceQuantity: String? = null, // 잔고수량
    @SerialName("last_alct_qty")
    val lastAllocationQuantity: String? = null, // 최종배정수량
    @SerialName("excs_alct_qty")
    val excessAllocationQuantity: String? = null, // 초과배정수량
    @SerialName("tot_alct_qty")
    val totalAllocationQuantity: String? = null, // 총배정수량
    @SerialName("last_ftsk_qty")
    val lastFractionalShareQuantity: String? = null, // 최종단수주수량
    @SerialName("last_alct_amt")
    val lastAllocationAmount: String? = null, // 최종배정금액
    @SerialName("last_ftsk_chgs")
    val lastFractionalShareCharges: String? = null, // 최종단수주대금
    @SerialName("rdpt_prca")
    val redemptionPrincipal: String? = null, // 상환원금
    @SerialName("dlay_int_amt")
    val delayInterestAmount: String? = null, // 지연이자금액
    @SerialName("lstg_dt")
    val listingDate: String? = null, // 상장일자
    @SerialName("sbsc_end_dt")
    val subscriptionEndDate: String? = null, // 청약종료일자
    @SerialName("cash_dfrm_dt")
    val cashPaymentDate: String? = null, // 현금지급일자
    @SerialName("rqst_qty")
    val requestQuantity: String? = null, // 신청수량
    @SerialName("rqst_amt")
    val requestAmount: String? = null, // 신청금액
    @SerialName("rqst_dt")
    val requestDate: String? = null, // 신청일자
    @SerialName("rfnd_dt")
    val refundDate: String? = null, // 환불일자
    @SerialName("rfnd_amt")
    val refundAmount: String? = null, // 환불금액
    @SerialName("lstg_stqt")
    val listingShareQuantity: String? = null, // 상장주수
    @SerialName("tax_amt")
    val taxAmount: String? = null, // 세금금액
    @SerialName("sbsc_unpr")
    val subscriptionUnitPrice: String? = null, // 청약단가
) {
    /**
     * 권리유형코드를 한글명으로 변환
     */
    fun getRightsTypeName(): String {
        return when (rightsTypeCode) {
            "01" -> "유상"
            "02" -> "무상"
            "03" -> "배당"
            "04" -> "매수청구"
            "05" -> "공개매수"
            "06" -> "주주총회"
            "07" -> "신주인수권증서"
            "08" -> "반대의사"
            "09" -> "신주인수권증권"
            "11" -> "합병"
            "12" -> "회사분할"
            "13" -> "주식교환"
            "14" -> "액면분할"
            "15" -> "액면병합"
            "16" -> "종목변경"
            "17" -> "감자"
            "18" -> "신구주합병"
            "21" -> "후합병"
            "22" -> "후회사분할"
            "23" -> "후주식교환"
            "24" -> "후액면분할"
            "25" -> "후액면병합"
            "26" -> "후종목변경"
            "27" -> "후감자"
            "28" -> "후신구주합병"
            "31" -> "뮤츄얼펀드"
            "32" -> "ETF"
            "33" -> "선박투자회사"
            "34" -> "투융자회사"
            "35" -> "해외자원"
            "36" -> "부동산신탁(Ritz)"
            "37" -> "상장수익증권"
            "41" -> "ELW만기"
            "42" -> "ELS분배"
            "43" -> "DLS분배"
            "44" -> "하일드펀드"
            "45" -> "ETN"
            "51" -> "전환청구"
            "52" -> "교환청구"
            "53" -> "BW청구"
            "54" -> "WRT청구"
            "55" -> "채권풋옵션청구"
            "56" -> "전환우선주청구"
            "57" -> "전환조건부청구"
            "58" -> "전자증권일괄입고"
            "59" -> "클라우드펀딩일괄입고"
            "61" -> "원리금상환"
            "62" -> "스트립채권"
            "71" -> "WRT소멸"
            "72" -> "WRT증권"
            "73" -> "DR전환"
            "74" -> "배당옵션"
            "75" -> "특별배당"
            "76" -> "ISINCODE변경"
            "77" -> "실권주청약"
            "81" -> "해외분배금(청산)"
            "82" -> "해외분배금(조기상환)"
            "83" -> "해외분배금(상장폐지)"
            "84" -> "DR FEE"
            "85" -> "SECTION 871M"
            "86" -> "종목전환"
            "87" -> "재매수"
            "88" -> "종목교환"
            "89" -> "기타이벤트"
            "91" -> "공모주"
            "92" -> "청약"
            "93" -> "환매"
            "99" -> "기타권리사유"
            else -> rightsTypeCode ?: "알 수 없음"
        }
    }
}

