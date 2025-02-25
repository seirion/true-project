package com.trueedu.project.dart.di

import com.trueedu.project.repository.local.Local
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class DartTokenInterceptor @Inject constructor(
    private val local: Local,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = local.dartApiKey
        val originalHttpUrl = chain.request().url
        val newHttpUrl = originalHttpUrl.newBuilder()
            .addQueryParameter("crtfc_key", apiKey)
            .build()
        val request = chain.request().newBuilder()
            .url(newHttpUrl)
            .build()

        return chain.proceed(request)
    }
}
