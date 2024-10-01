package com.trueedu.project.data

import com.trueedu.project.model.dto.auth.TokenResponse
import com.trueedu.project.repository.local.Local
import com.trueedu.project.utils.parseDateString
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserInfo @Inject constructor(
    val local: Local,
) {
    fun setAccessToken(tokenResponse: TokenResponse) {
        local.accessToken = tokenResponse.accessToken
        local.accessTokenExpiredAt = parseDateString(tokenResponse.accessTokenTokenExpired)?.time ?: 0L
    }

    fun hasValidToken(): Boolean {

        if (local.accessToken.isEmpty()) return false

        val tokenExpirationTime = Date(local.accessTokenExpiredAt)
        val calendar = Calendar.getInstance()
        calendar.time = tokenExpirationTime
        calendar.add(Calendar.MINUTE, -5) // 5 minutes to the token expiration time
        val bufferedExpirationTime = calendar.time
        val currentTime = Date()

        return bufferedExpirationTime.after(currentTime)
    }

}
