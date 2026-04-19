package com.trueedu.project.network

import com.trueedu.project.di.TokenRefreshService
import com.trueedu.project.model.dto.auth.TokenRequest
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.AuthService
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * KIS API는 토큰 만료 시 HTTP 200/500 + body { "rt_cd": "1", "msg_cd": "EGW00123" }을 반환.
 * OkHttp Authenticator(401 전용)로는 감지 불가하므로, 응답 body를 검사해 갱신 후 재시도한다.
 *
 * 동시성: @Synchronized로 중복 갱신 방지 (여러 요청이 동시에 만료 감지해도 한 번만 갱신).
 */
@Singleton
class TokenRefreshInterceptor @Inject constructor(
    @TokenRefreshService private val authService: AuthService,
    private val local: Local,
) : Interceptor {

    companion object {
        private const val EXPIRED_MSG_CD = "EGW00123"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())

        // 토큰 만료 코드 감지 (body를 소비하지 않도록 peekBody 사용)
        if (!isTokenExpired(response)) return response

        response.close()

        // 토큰 갱신 (동기, 동시 갱신 방지)
        val refreshed = refreshTokenSync() ?: return chain.proceed(chain.request())

        // 새 토큰으로 헤더 교체 후 재시도
        val newRequest = chain.request().newBuilder()
            .header("authorization", "Bearer $refreshed")
            .build()
        return chain.proceed(newRequest)
    }

    private fun isTokenExpired(response: Response): Boolean {
        return try {
            val body = response.peekBody(4096).string()
            val json = Json { ignoreUnknownKeys = true }
            val obj = json.parseToJsonElement(body).jsonObject
            val msgCd = obj["msg_cd"]?.jsonPrimitive?.content
            msgCd == EXPIRED_MSG_CD
        } catch (e: Exception) {
            false
        }
    }

    @Synchronized
    private fun refreshTokenSync(): String? {
        // 다른 스레드가 이미 갱신했을 수 있으므로 현재 토큰이 바뀌었는지 확인
        val currentToken = local.accessToken

        return runBlocking {
            val userKey = local.getUserKeys().lastOrNull() ?: return@runBlocking null
            val request = TokenRequest(
                grantType = "client_credentials",
                appKey = userKey.appKey,
                appSecret = userKey.appSecret,
            )
            val result = runCatching { authService.refreshToken(request) }.getOrNull()
                ?: return@runBlocking null

            if (result.isSuccessful) {
                val tokenResponse = result.body() ?: return@runBlocking null
                local.setAccessToken(tokenResponse)
                tokenResponse.accessToken
            } else {
                null
            }
        }
    }
}
