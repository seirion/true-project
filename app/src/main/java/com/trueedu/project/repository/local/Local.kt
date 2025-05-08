package com.trueedu.project.repository.local

import android.content.SharedPreferences
import com.trueedu.project.extensions.boolean
import com.trueedu.project.extensions.int
import com.trueedu.project.extensions.long
import com.trueedu.project.extensions.string
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.ui.views.order.OrderTab
import com.trueedu.project.ui.views.schedule.OrderSchedule
import com.trueedu.project.utils.parseDateString
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Local @Inject constructor(private val preferences: SharedPreferences) {
    private val latestVersion = 1
    private var currentVersion by preferences.int(latestVersion)
    var launchingCount by preferences.long(0) // 앱 실행 횟수
        private set

    fun migrate() {
        launchingCount++
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    // appKey, appSecret, accountNumber, htsId
    // 마지막 element 가 최근에 사용한 값임
    var userKeys by preferences.string("[]")
    fun getUserKeys(): List<UserKey> {
        return try {
            json.decodeFromString<List<UserKey>>(userKeys)
        } catch (e: SerializationException) {
            emptyList()
        }
    }

    var accessToken by preferences.string("")
        private set
    // 토큰 만료 예정 시각
    var accessTokenExpiredAt by preferences.long(0L)
        private set
    fun setAccessToken(tokenResponse: TokenResponse?) {
        if (tokenResponse == null) {
            accessToken = ""
            accessTokenExpiredAt = 0L
        } else {
            accessToken = tokenResponse.accessToken
            accessTokenExpiredAt = parseDateString(tokenResponse.accessTokenTokenExpired)?.time ?: 0L
        }
    }

    var webSocketKey by preferences.string("")

    // dart 정보
    var dartApiKey by preferences.string("")

    // 사용자 계정 정보

    // 면책 조항 표시
    var disclaimerVisible by preferences.boolean(true)

    // 확인한 notice 마지막 id
    var appNoticeId by preferences.int(0)

    // UI
    var forceDark by preferences.boolean(false)
    var theme by preferences.int(1)
    var keepScreenOn by preferences.boolean(false)

    // 홈 설정
    var dailyProfitMode by preferences.boolean(false) // 사용 안 함
    var marketPriceMode by preferences.boolean(false) // 시세 | 수익

    // 거래
    private var orderTab by preferences.string("Order")
    fun getOrderTab(): OrderTab {
        return OrderTab.valueOf(orderTab)
    }
    fun setOrderTab(tab: OrderTab) {
        orderTab = tab.name
    }

    // 종목 다운로드 시각 yyyyMMddHHmm
    var stockUpdatedAt by preferences.long(0L)

    // 스팩 설정
    var spacAnnualProfit by preferences.boolean(false) // 청산 가치 1년 환산 표시

    // 예약 매매
    private var orderScheduleJson by preferences.string("{}")

    fun setOrderSchedule(list: List<OrderSchedule>) {
        orderScheduleJson = json.encodeToString(list)
    }

    fun getOrderSchedule(): List<OrderSchedule> {
        return try {
            val jsonString = orderScheduleJson
            json.decodeFromString<List<OrderSchedule>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // 임시 - 테스트 결과 쓰기
    var orderResult by preferences.string("")
}
