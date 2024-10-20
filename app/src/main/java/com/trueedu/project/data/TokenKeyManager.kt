package com.trueedu.project.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.model.dto.auth.WebSocketKeyRequest
import com.trueedu.project.model.event.TokenIssueFail
import com.trueedu.project.model.event.TokenIssued
import com.trueedu.project.model.event.TokenKeyEvent
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
        private val TAG = TokenKeyManager::class.java.simpleName

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
        issueAccessToken()
        issueWebSocketKey()
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
            Log.d(TAG, "websocket key exists: ${local.webSocketKey}")
            return
        }
        if (appKey.isEmpty() || appSecret.isEmpty()) {
            Log.d(TAG, "appKey appSecret is empty")
            return
        }
        val request = WebSocketKeyRequest(
            grantType = "client_credentials",
            appKey = appKey,
            secretKey = appSecret,
        )

        authRemote.webSocketKey(request)
            .catch {
                Log.e(TAG, "failed to get websocket key: $it")
            }
            .onEach {
                local.webSocketKey = it.approvalKey
                event.emit(WebSocketKeyIssued())
                Log.d(TAG, "new web socket key: $it")
            }
            .launchIn(MainScope())
    }

    private fun issueAccessToken() {
        Log.d(TAG, "issueAccessToken()")
        val appKey = userKey.value?.appKey
        val appSecret = userKey.value?.appSecret
        if (appKey.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            Log.d(TAG, "appKey appSecret is empty")
            return
        }
        if (hasValidToken()) {
            Log.d(TAG, "token is valid")
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
                Log.e(TAG, "failed to get AccessToken: $it")
                event.emit(TokenIssueFail())
            }
            .onEach {
                setAccessToken(it)
                event.emit(TokenIssued())
                Log.d(TAG, "new token: $it")
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
                Log.e(TAG, "failed to revoke AccessToken: $it")
                // service not available
            }
            .onEach {
                event.emit(TokenRevoked())
                Log.d(TAG, "revoke ok: $it")
            }
            .launchIn(MainScope())
    }

    fun clearToken() {
        local.setAccessToken(null)
    }

    fun setAccessToken(tokenResponse: TokenResponse) {
        local.setAccessToken(tokenResponse)
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
        val list = getUserKeys().filter {
            it.accountNum != userKey.accountNum
        }

        val jsonString = json.encodeToString(list + userKey)
        local.userKeys = jsonString
        this.userKey.value = userKey

        // 키 정보가 갱신되면 토큰을 재발급 받아야 함
        clearToken()
        local.webSocketKey = ""
        issueAccessToken()
        issueWebSocketKey()
    }

    fun deleteUserKey(accountNum: String) {
        val userKeys = getUserKeys()
        val newUserKeys = userKeys.filter { it.accountNum != accountNum }

        if (userKeys.size == newUserKeys.size) {
            Log.d(TAG, "not exists userKey: $accountNum")
            return
        }

        val jsonString = json.encodeToString(newUserKeys)
        local.userKeys = jsonString

        // userKey 가 갱신되는 경우
        val newKey = newUserKeys.lastOrNull()
        if (newKey != userKey.value) {
            this.userKey.value = newKey

            // 키 정보가 갱신되면 토큰을 재발급 받아야 함
            clearToken()
            local.webSocketKey = ""
            if (newKey != null) {
                issueAccessToken()
                issueWebSocketKey()
            }
        }
    }
}
