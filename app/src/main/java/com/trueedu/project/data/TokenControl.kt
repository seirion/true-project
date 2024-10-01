package com.trueedu.project.data

import android.util.Log
import com.trueedu.project.model.dto.auth.RevokeTokenRequest
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.AuthRemote
import com.trueedu.project.utils.parseDateString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenControl @Inject constructor(
    private val local: Local,
    private val authRemote: AuthRemote,
) {
    companion object {
        private val TAG = TokenControl::class.java.simpleName
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

    fun issueAccessToken() {
        Log.d(TAG, "appKey: ${local.appKey}")
        Log.d(TAG, "appSecret: ${local.appSecret}")
        issueAccessToken(appKey = local.appKey, appSecret = local.appSecret)
    }

    fun issueAccessToken(
        appKey: String,
        appSecret: String,
        onSuccess: (TokenResponse) -> Unit = {},
        onFailed: (Throwable) -> Unit = {},
    ) {
        if (appKey.isEmpty() || appSecret.isEmpty()) {
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
                withContext(Dispatchers.Main) {
                    onFailed(it)
                }
            }
            .onEach {
                setAccessToken(it)
                withContext(Dispatchers.Main) {
                    onSuccess(it)
                }
                Log.d(TAG, "new token: $it")
            }
            .launchIn(MainScope())
    }

    private fun revokeToken() {
        if (local.accessToken.isEmpty()) {
            return
        }

        val request = RevokeTokenRequest(
            appKey = local.appKey,
            appSecret = local.appSecret,
            token = local.accessToken,
        )
        authRemote.revokeToken(request)
            .catch {
                Log.e(TAG, "failed to revoke AccessToken: $it")
                // service not available
            }
            .onEach {
                Log.d(TAG, "revoke ok: $it")
            }
            .launchIn(MainScope())
    }

    fun setAccessToken(tokenResponse: TokenResponse) {
        local.accessToken = tokenResponse.accessToken
        local.accessTokenExpiredAt = parseDateString(tokenResponse.accessTokenTokenExpired)?.time ?: 0L
    }
}