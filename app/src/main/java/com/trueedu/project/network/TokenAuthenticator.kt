package com.trueedu.project.network

import android.content.Context
import android.util.Log
import com.trueedu.project.di.TokenRefreshService
import com.trueedu.project.model.dto.TokenRequest
import com.trueedu.project.model.dto.TokenResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.repository.remote.service.AuthService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenAuthenticator @Inject constructor(
    @ApplicationContext private val context: Context,
    @TokenRefreshService private val authService: AuthService,
    private val local: Local,
) : Authenticator {
    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        return runBlocking {
            val request = TokenRequest(
                grantType = "client_credentials",
                appKey = local.appKey,
                appSecret = local.appSecret,
            )
            val tokenResponse = authService.refreshToken(request)
            val responseCode = tokenResponse.errorBody()?.string()?.let {
                runCatching {
                    val jsonElement = Json.parseToJsonElement(it)
                    jsonElement.jsonObject["code"]?.jsonPrimitive?.intOrNull
                }
                    .onFailure {
                        //recordException(it)
                    }
                    .getOrNull()
            }

            if (tokenResponse.isSuccessful) {
                val newToken = tokenResponse.body()
                saveNewToken(newToken!!)
                return@runBlocking response.request.newBuilder()
                    .header("Authorization", "Bearer ${newToken.accessToken}")
                    .build()
            } else {
                clearUser()
                return@runBlocking null
            }
        }
    }

    private fun clearUser() {
        local.accessToken = ""
        local.refreshToken = ""
    }

    private fun saveNewToken(tokenResponse: TokenResponse) {
        local.accessToken = tokenResponse.accessToken
    }
}
