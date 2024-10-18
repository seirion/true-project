package com.trueedu.project.repository.local

import android.content.SharedPreferences
import com.trueedu.project.extensions.boolean
import com.trueedu.project.extensions.int
import com.trueedu.project.extensions.long
import com.trueedu.project.extensions.string
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.model.local.UserKey
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

    // not used
    //private var appKey by preferences.string("")
    //private var appSecret by preferences.string("")

    // appKey, appSecret, accountNumber, htsId
    private var userKeys by preferences.string("[]")
    fun getUserKeys(): List<UserKey> {
        return try {
            json.decodeFromString<List<UserKey>>(userKeys)
        } catch (e: SerializationException) {
            emptyList()
        }
    }

    // 마지막에 추가
    fun addUserKey(userKey: UserKey) {
        val list = getUserKeys().filter {
            it.accountNum != userKey.accountNum
        }

        val jsonString = json.encodeToString(list + userKey)
        userKeys = jsonString
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

    // 사용자 계정 정보

    // 사용자 전체 계좌 목록 - comma separated list
    private var accountNumbers by preferences.string("")
    val accountNumberList: List<String>
        get() = accountNumbers.split(",")
    fun setAccountNumberList(list: List<String>) {
        accountNumbers = list.joinToString(",")
    }

    // 사용자가 가장 최근 선택한 계좌 번호
    var currentAccountNumber by preferences.string("")

    // UI
    var forceDark by preferences.boolean(false)
    var theme by preferences.int(1)
    var keepScreenOn by preferences.boolean(false)

    // 홈 설정
    var dailyProfitMode by preferences.boolean(false) // 사용 안 함
    var marketPriceMode by preferences.boolean(false) // 시세 | 수익

}
