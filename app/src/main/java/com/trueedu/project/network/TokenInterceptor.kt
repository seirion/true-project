package com.trueedu.project.network

import com.trueedu.project.di.AppVersion
import com.trueedu.project.di.AppVersionCode
import com.trueedu.project.di.getApiHeaders
import com.trueedu.project.repository.local.Local
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val local: Local,
    @AppVersion private val appVersion: String?,
    @AppVersionCode private val appVersionCode: Long
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = local.accessToken

        val headers = getApiHeaders(accessToken, appVersion = appVersion, appVersionCode = appVersionCode)

        val request = chain.request().newBuilder()
            .headers(headers)
            .build()

        return chain.proceed(request)
    }
}
