package com.trueedu.project.network

import com.trueedu.project.di.getApiHeaders
import com.trueedu.project.repository.local.Local
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val local: Local,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val appKey = local.appKey
        val appSecret = local.appSecret
        // val accessToken = local.accessToken

        val headers = getApiHeaders(appKey, appSecret)

        val request = chain.request().newBuilder()
            .headers(headers)
            .build()

        return chain.proceed(request)
    }
}
