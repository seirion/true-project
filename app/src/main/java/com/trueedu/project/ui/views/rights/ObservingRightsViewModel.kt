package com.trueedu.project.ui.views.rights

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.trueedu.project.data.TokenKeyManager
import com.trueedu.project.data.log.logD
import com.trueedu.project.data.log.logE
import com.trueedu.project.data.log.logW
import com.trueedu.project.model.dto.order.RightsDetail
import com.trueedu.project.model.dto.order.RightsResponse
import com.trueedu.project.repository.remote.OrderRemote
import com.trueedu.project.utils.yyyyMMdd
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ObservingRightsViewModel @Inject constructor(
    private val tokenKeyManager: TokenKeyManager,
    private val orderRemote: OrderRemote,
): ViewModel() {
    companion object {
        private const val MAX_PAGE_COUNT = 50
    }

    data class RightsPopup(
        val title: String,
        val desc: String,
    )

    val loading = mutableStateOf(false)

    /**
     * 화면에 남겨둘 최신 결과(팝업 닫은 뒤에도 확인 가능).
     */
    val lastResult = mutableStateOf<RightsPopup?>(null)

    fun fetchRights() {
        if (loading.value) return

        val userKey = tokenKeyManager.userKey.value
        if (userKey?.accountNum.isNullOrBlank()) {
            val popup = RightsPopup(
                title = "권리 현황 조회",
                desc = "권리 현황 조회를 위해서는 앱키 등록이 필요합니다.",
            )
            lastResult.value = popup
            return
        }

        viewModelScope.launch {
            loading.value = true
            try {
                val now = LocalDate.now()
                val fromDate = now.minusMonths(3).yyyyMMdd()
                val toDate = now.yyyyMMdd()

                logD("권리 현황 조회 시작: $fromDate ~ $toDate")
                val response = fetchRightsAllPages(
                    accountNum = userKey?.accountNum ?: "",
                    fromDate = fromDate,
                    toDate = toDate,
                )

                val rawList = response.list.orEmpty()
                val filteredList = rawList
                    .filter { it.lastAllocationAmount.toAmountOrNull()?.let { amount -> amount != 0L } == true }
                    .sortedBy { it.cashPaymentDate ?: "99999999" }

                val popup = RightsPopup(
                    title = "권리 현황 조회 결과",
                    desc = buildRightsPopupDesc(
                        fromDate = fromDate,
                        toDate = toDate,
                        responseMsg = response.msg,
                        totalCount = rawList.size,
                        list = filteredList,
                    )
                )
                lastResult.value = popup
            } catch (e: Exception) {
                logE(e, "권리 현황 조회 에러: ${e.message}")
                val popup = RightsPopup(
                    title = "권리 현황 조회 실패",
                    desc = e.message ?: "알 수 없는 오류",
                )
                lastResult.value = popup
            } finally {
                loading.value = false
            }
        }
    }

    /**
     * 기간별 계좌 권리 현황 조회: 연속조회(fk100/nk100)로 전체 페이지를 합산합니다.
     */
    private suspend fun fetchRightsAllPages(
        accountNum: String,
        fromDate: String,
        toDate: String,
    ): RightsResponse {
        val combinedList = mutableListOf<RightsDetail>()
        var fk100 = ""
        var nk100 = ""
        var lastFk100: String? = null
        var lastNk100: String? = null
        var lastResponse: RightsResponse? = null

        repeat(MAX_PAGE_COUNT) { pageIndex ->
            val response = orderRemote.periodRights(
                accountNum = accountNum,
                fromDate = fromDate,
                toDate = toDate,
                code = "", // 전체 조회
                fk100 = fk100,
                nk100 = nk100,
            ).first()

            if (response.rtCd != "0") {
                throw IllegalStateException("${response.msgCd}: ${response.msg}")
            }

            lastResponse = response
            combinedList.addAll(response.list.orEmpty())

            val nextFk100 = response.fk100.orEmpty()
            val nextNk100 = response.nk100.orEmpty()
            val hasNext = nextFk100.isNotBlank() && nextNk100.isNotBlank()

            if (!hasNext) return response.copy(list = combinedList)

            // 키가 더 이상 변하지 않으면 무한 루프 방지
            if (lastFk100 == nextFk100 && lastNk100 == nextNk100) {
                logW("연속조회 키가 변하지 않아 중단합니다. page=${pageIndex + 1}, fk100=$nextFk100, nk100=$nextNk100")
                return response.copy(list = combinedList)
            }

            lastFk100 = nextFk100
            lastNk100 = nextNk100
            fk100 = nextFk100
            nk100 = nextNk100
        }

        val response = lastResponse ?: throw IllegalStateException("권리 현황 응답이 없습니다.")
        logW("연속조회 최대 페이지 수(${MAX_PAGE_COUNT})에 도달하여 중단합니다.")
        return response.copy(list = combinedList)
    }

    private fun String?.toAmountOrNull(): Long? {
        return this
            ?.replace(",", "")
            ?.trim()
            ?.toLongOrNull()
    }

    private fun buildRightsPopupDesc(
        fromDate: String,
        toDate: String,
        responseMsg: String,
        totalCount: Int,
        list: List<RightsDetail>,
    ): String {
        if (list.isEmpty()) {
            return """
                조회 기간: $fromDate ~ $toDate
                응답: $responseMsg

                조회된 권리 내역이 없습니다.
            """.trimIndent()
        }

        return buildString {
            appendLine("조회 기간: $fromDate ~ $toDate")
            appendLine("응답: $responseMsg")
            appendLine("전체: ${totalCount}건 / 표시: ${list.size}건")
            appendLine()
            list.forEachIndexed { index, item ->
                appendLine("${index + 1}. ${item.nameKr ?: "N/A"} (${item.code ?: "N/A"})")
                appendLine("   - 유형: ${item.getRightsTypeName()} (코드: ${item.rightsTypeCode ?: "N/A"})")
                appendLine("   - 기준일: ${item.baseDate ?: "N/A"} / 현금지급일: ${item.cashPaymentDate ?: "N/A"}")
                appendLine("   - 최종배정금액: ${item.lastAllocationAmount ?: "N/A"} / 세금: ${item.taxAmount ?: "N/A"}")
                if (index != list.lastIndex) appendLine()
            }
        }.trimEnd()
    }
}


