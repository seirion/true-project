package com.trueedu.project.data

import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.data.log.logD
import com.trueedu.project.data.log.logE
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.model.dto.auth.WebSocketKeyRequest
import com.trueedu.project.model.event.TokenIssueFail
import com.trueedu.project.model.event.TokenIssued
import com.trueedu.project.model.event.TokenKeyEvent
import com.trueedu.project.model.event.TokenOk
import com.trueedu.project.model.event.TokenRevoked
import com.trueedu.project.model.event.WebSocketKeyIssued
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenKeyManager @Inject constructor(
    private val local: Local,
    private val authRemote: AuthRemote,
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
    }

    val userKey = mutableStateOf<UserKey?>(null)

    // auth 관련 이벤트 구독을 위함
    private val event = MutableSharedFlow<TokenKeyEvent>(1)

    fun observeTokenKeyEvent(): Flow<TokenKeyEvent> {
        return event
    }

    init {
        userKey.value = getUserKeys().lastOrNull()

        if (userKey.value != null) {
            issueAccessToken()
            issueWebSocketKey()
        }
    }

    private fun hasValidToken(): Boolean {

        if (local.accessToken.isEmpty()) return false

        val tokenExpirationTime = Date(local.accessTokenExpiredAt)
        val calendar = Calendar.getInstance()
        calendar.time = tokenExpirationTime
        calendar.add(Calendar.MINUTE, -5) // 5 minutes to the token expiration time
        val bufferedExpirationTime = calendar.time
        val currentTime = Date()

        return bufferedExpirationTime.after(currentTime)
    }

    private fun issueWebSocketKey() {
        val appKey = userKey.value!!.appKey!!
        val appSecret = userKey.value!!.appSecret!!
        if (local.webSocketKey.isNotEmpty()) {
            logD("websocket key exists: ${local.webSocketKey}")
            return
        }
        if (appKey.isEmpty() || appSecret.isEmpty()) {
            logD("appKey appSecret is empty")
            return
        }
        val request = WebSocketKeyRequest(
            grantType = "client_credentials",
            appKey = appKey,
            secretKey = appSecret,
        )

        authRemote.webSocketKey(request)
            .catch {
                logE("failed to get websocket key: $it")
            }
            .onEach {
                local.webSocketKey = it.approvalKey
                event.emit(WebSocketKeyIssued)
                logD("new web socket key: $it")
            }
            .launchIn(MainScope())
    }

    private fun issueAccessToken() {
        logD("issueAccessToken()")
        val appKey = userKey.value?.appKey
        val appSecret = userKey.value?.appSecret
        if (appKey.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            logD("appKey appSecret is empty")
            return
        }
        if (hasValidToken()) {
            logD("token is valid")
            MainScope().launch {
                event.emit(TokenOk)
            }
            return
        }

        val request = TokenRequest(
            grantType = "client_credentials",
            appKey = appKey,
            appSecret = appSecret,
        )

        authRemote.refreshToken(request)
            .catch {
                // service not available
                logE("failed to get AccessToken: $it")
                event.emit(TokenIssueFail)
            }
            .onEach {
                setAccessToken(it)
                event.emit(TokenIssued)
                logD("new token: $it")
            }
            .launchIn(MainScope())
    }

    private fun revokeToken() {
        if (userKey.value?.appKey == null || userKey.value?.appSecret == null) return

        if (local.accessToken.isEmpty()) {
            return
        }

        val request = RevokeTokenRequest(
            appKey = userKey.value!!.appKey!!,
            appSecret = userKey.value!!.appSecret!!,
            token = local.accessToken,
        )
        authRemote.revokeToken(request)
            .catch {
                logE("failed to revoke AccessToken: $it")
                // service not available
            }
            .onEach {
                event.emit(TokenRevoked)
                logD("revoke ok: $it")
            }
            .launchIn(MainScope())
    }

    fun clearToken() {
        local.setAccessToken(null)
    }

    fun setAccessToken(tokenResponse: TokenResponse) {
        local.setAccessToken(tokenResponse)
        // 발급된 토큰을 현재 계정에 함께 저장해 두기
        saveTokenToCurrentUserKey()
    }

    /** 현재 local 토큰을 userKeys 목록에서 현재 계정에 저장 */
    private fun saveTokenToCurrentUserKey() {
        val current = userKey.value ?: return
        val list = getUserKeys()
        val updated = list.map {
            if (it.accountNum == current.accountNum) {
                it.copy(
                    accessToken = local.accessToken.takeIf { t -> t.isNotEmpty() },
                    accessTokenExpiredAt = local.accessTokenExpiredAt.takeIf { t -> t > 0L },
                )
            } else it
        }
        local.userKeys = json.encodeToString(updated)
    }

    /** 선택된 계정의 저장된 토큰을 Local 에 복원하고, 만료됐거나 없으면 새로 발급 */
    private fun restoreOrIssueToken(key: UserKey) {
        val cachedToken = key.accessToken
        val cachedExpiredAt = key.accessTokenExpiredAt ?: 0L

        if (!cachedToken.isNullOrEmpty() && cachedExpiredAt > 0L) {
            // 캐시된 토큰을 Local 에 복원
            local.setAccessToken(cachedToken, cachedExpiredAt)
            if (hasValidToken()) {
                logD("restoring cached token for ${key.accountNum}")
                MainScope().launch { event.emit(TokenOk) }
                return
            }
        }
        // 만료됐거나 없는 경우 새로 발급
        issueAccessToken()
    }

    fun getUserKeys(): List<UserKey> {
        return try {
            json.decodeFromString<List<UserKey>>(local.userKeys)
        } catch (e: SerializationException) {
            emptyList()
        }
    }

    // 마지막에 추가
    fun addUserKey(userKey: UserKey) {
        // 현재 계정의 토큰을 저장해 두기
        saveTokenToCurrentUserKey()

        val list = getUserKeys().filter {
            it.accountNum != userKey.accountNum
        }
        val jsonString = json.encodeToString(list + userKey)
        local.userKeys = jsonString
        this.userKey.value = userKey

        local.webSocketKey = ""
        // 선택한 계정의 캐시된 토큰 복원 or 새 발급
        restoreOrIssueToken(userKey)
        issueWebSocketKey()
    }

    fun deleteUserKey(accountNum: String) {
        // 삭제 전에 현재 계정 토큰 저장
        saveTokenToCurrentUserKey()

        val userKeys = getUserKeys()
        val newUserKeys = userKeys.filter { it.accountNum != accountNum }

        if (userKeys.size == newUserKeys.size) {
            logD("not exists userKey: $accountNum")
            return
        }

        val jsonString = json.encodeToString(newUserKeys)
        local.userKeys = jsonString

        // userKey 가 갱신되는 경우
        val newKey = newUserKeys.lastOrNull()
        if (newKey != userKey.value) {
            this.userKey.value = newKey

            local.webSocketKey = ""
            if (newKey != null) {
                restoreOrIssueToken(newKey)
                issueWebSocketKey()
            } else {
                clearToken()
            }
        }
    }
}
