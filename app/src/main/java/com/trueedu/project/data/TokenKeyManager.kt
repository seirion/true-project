package com.trueedu.project.data

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.model.dto.auth.WebSocketKeyRequest
import com.trueedu.project.model.event.AuthEvent
import com.trueedu.project.model.event.TokenIssued
import com.trueedu.project.model.event.TokenRevoked
import com.trueedu.project.model.event.WebSocketKeyIssued
import com.trueedu.project.model.local.UserKey
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    private val event = MutableSharedFlow<AuthEvent>(1)

    fun observeAuthEvent(): Flow<AuthEvent> {
        return event
    }

    init {
        userKey.value = getUserKeys().lastOrNull()
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

    fun issueAccessToken(onSuccess: () -> Unit) {
        Log.d(TAG, "appKey: ${userKey.value?.appKey}")
        Log.d(TAG, "appSecret: ${userKey.value?.appSecret}")
        issueAccessToken(
            appKey = userKey.value?.appKey,
            appSecret = userKey.value?.appSecret,
            onSuccess = onSuccess
        )
    }

    fun issueWebSocketKey(onSuccess: () -> Unit) {
        if (userKey.value?.appKey == null || userKey.value?.appSecret == null) return

        issueWebSocketKey(
            appKey = userKey.value!!.appKey!!,
            appSecret = userKey.value!!.appSecret!!,
            onSuccess = onSuccess
        )
    }

    private fun issueWebSocketKey(
        appKey: String,
        appSecret: String,
        onSuccess: () -> Unit,
    ) {
        if (local.webSocketKey.isNotEmpty()) {
            Log.d(TAG, "websocket key exists: ${local.webSocketKey}")
            onSuccess()
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
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
                Log.d(TAG, "new web socket key: $it")
            }
            .launchIn(MainScope())
    }

    fun issueAccessToken(
        appKey: String?,
        appSecret: String?,
        onSuccess: () -> Unit = {},
        onFailed: (Throwable) -> Unit = {},
    ) {
        if (appKey.isNullOrEmpty() || appSecret.isNullOrEmpty()) {
            Log.d(TAG, "appKey appSecret is empty")
            return
        }
        if (hasValidToken()) {
            Log.d(TAG, "token is valid")
            MainScope().launch {
                onSuccess()
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
                Log.e(TAG, "failed to get AccessToken: $it")
                withContext(Dispatchers.Main) {
                    onFailed(it)
                }
            }
            .onEach {
                setAccessToken(it)
                event.emit(TokenIssued())
                withContext(Dispatchers.Main) {
                    onSuccess()
                }
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
    }
}
